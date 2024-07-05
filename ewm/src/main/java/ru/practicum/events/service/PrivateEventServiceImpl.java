package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dao.CategoryRepository;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dao.EventRepository;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventRequestStatusUpdateDto;
import ru.practicum.events.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUserUpdateDto;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.EventState;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dao.ParticipationRequestRepository;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.mapper.RequestMapper;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.users.dao.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;

    @Override
    public List<EventShortDto> getAll(long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var events = eventRepository.findAllByInitiatorId(userId, pageable);
        return eventMapper.toShortDto(events);
    }

    @Override
    public EventFullDto create(long userId, EventCreateDto eventCreateDto) {
        var initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var category = categoryRepository.findById(eventCreateDto.getCategory())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(
                        eventCreateDto.getCategory()
                )));
        var event = eventRepository.save(
                eventMapper.toModel(eventCreateDto, initiator, category,
                        LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS), EventState.PENDING)
        );
        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto get(long userId, long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));
        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto update(long userId, long eventId, EventUserUpdateDto updateEventDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        Category category = null;
        if (updateEventDto.getCategory() != null) {
            category = categoryRepository.findById(updateEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(updateEventDto.getCategory()))
                    );
        }
        var oldEvent = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));
        if (EventState.PUBLISHED == oldEvent.getState()) {
            throw new AccessDeniedException(ErrorMessages.EVENT_PUBLISHED.getMessage());
        }
        eventMapper.toModel(oldEvent, updateEventDto, category);
        if (updateEventDto.getStateAction() != null) {
            switch (updateEventDto.getStateAction()) {
                case SEND_TO_REVIEW:
                    oldEvent.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    oldEvent.setState(EventState.CANCELED);
                    break;
            }
        }
        return eventMapper.toFullDto(eventRepository.save(oldEvent));
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