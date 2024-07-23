package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.dao.EventRepository;
import ru.practicum.events.dto.EventRequestStatusUpdateDto;
import ru.practicum.events.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.events.model.EventState;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dao.ParticipationRequestRepository;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.users.dao.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAll(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        return requestMapper.toDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(long userId, long eventId) {
        var requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));
        if (EventState.PUBLISHED != event.getState()) {
            throw new AccessDeniedException(ErrorMessages.EVENT_NOT_PUBLISHED.getMessage());
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedException(ErrorMessages.REQUESTER_IS_INITIATOR.getMessage());
        }
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <=
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new AccessDeniedException(ErrorMessages.PARTICIPANT_LIMIT_REACHED.getMessage());
        }

        var request = ParticipationRequest.builder()
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .requester(requester)
                .event(event)
                .status(!event.getRequestModeration() || event.getParticipantLimit() == 0 ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .build();
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto update(long userId, long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var oldRequest = requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(
                () -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(requestId)));
        oldRequest.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(oldRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(long userId, long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));
        var result = requestRepository.findAllByEventId(eventId);
        return requestMapper.toDto(result);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateRequest(long userId, long eventId, EventRequestStatusUpdateDto statusUpdateRequest) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return new EventRequestStatusUpdateResultDto();
        }
        var confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmedRequests >= event.getParticipantLimit()) {
            throw new AccessDeniedException(ErrorMessages.PARTICIPANT_LIMIT_REACHED.getMessage());
        }
        var requests = requestRepository.findAllByEventIdAndIdIn(eventId, statusUpdateRequest.getRequestIds());
        if (requests.stream().anyMatch(r -> RequestStatus.PENDING != r.getStatus())) {
            throw new AccessDeniedException(ErrorMessages.REQUEST_NOT_PENDING.getMessage());
        }
        if (RequestStatus.REJECTED == statusUpdateRequest.getStatus()) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
        } else {
            for (var i = 0; i < statusUpdateRequest.getRequestIds().size(); ++i) {
                if (confirmedRequests < event.getParticipantLimit()) {
                    requests.get(i).setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests++;
                } else {
                    requests.get(i).setStatus(RequestStatus.REJECTED);
                }
            }
        }
        var result = new EventRequestStatusUpdateResultDto(new ArrayList<>(), new ArrayList<>());
        requestRepository.saveAll(requests).forEach(r -> {
            if (RequestStatus.REJECTED == r.getStatus()) {
                result.getRejectedRequests().add(requestMapper.toDto(r));
            } else {
                result.getConfirmedRequests().add(requestMapper.toDto(r));
            }
        });
        return result;
    }
}