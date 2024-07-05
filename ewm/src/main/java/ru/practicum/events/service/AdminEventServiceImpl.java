package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.categories.dao.CategoryRepository;
import ru.practicum.categories.model.Category;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.events.dao.EventRepository;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;
import ru.practicum.events.model.RequestsCount;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dao.ParticipationRequestRepository;
import ru.practicum.requests.model.RequestStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {

    private final ParticipationRequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> get(List<Long> users, List<EventState> states, List<Long> categories,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageRequest) {
        var specification = new Specification<Event>() {
            @Override
            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (users != null && !users.isEmpty()) {
                    predicates.add(root.get("initiator").in(users));
                }
                if (states != null && !states.isEmpty()) {
                    predicates.add(root.get("state").in(states));
                }
                if (categories != null && !categories.isEmpty()) {
                    predicates.add(root.get("category").in(categories));
                }
                if (rangeStart != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
                }
                if (rangeEnd != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        var events = eventRepository.findAll(specification, pageRequest).stream()
                .collect(Collectors.toList());
        if (events.isEmpty()) return Collections.emptyList();

        var eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        var confirmedRequests = getConfirmedRequests(eventIds);
        var viewStats = getViews(events);
        return events.stream()
                .map(e -> eventMapper.toFullDto(e, viewStats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto update(long eventId, EventAdminUpdateDto updateEventDto) {
        Category category = null;
        if (updateEventDto.getCategory() != null) {
            category = categoryRepository.findById(updateEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(updateEventDto.getCategory()))
                    );
        }
        var oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));

        if (updateEventDto.getStateAction() != null) {
            switch (updateEventDto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (oldEvent.getState() != EventState.PENDING) {
                        throw new AccessDeniedException(ErrorMessages.EVENT_NOT_PUBLISHED.getMessage());
                    }
                    oldEvent.setState(EventState.PUBLISHED);
                    oldEvent.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
                    break;
                case REJECT_EVENT:
                    if (oldEvent.getState() == EventState.PUBLISHED) {
                        throw new AccessDeniedException(ErrorMessages.EVENT_PUBLISHED.getMessage());
                    }
                    oldEvent.setState(EventState.CANCELED);
                    break;
                default:
                    break;
            }
        }
        eventMapper.update(oldEvent, updateEventDto, category);
        return eventMapper.toFullDto(eventRepository.save(oldEvent));
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return requestRepository.countAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(RequestsCount::getEventId, RequestsCount::getCount));
    }

    private Map<Long, Long> getViews(List<Event> events) {
        var startView = events.stream()
                .min(Comparator.comparing(Event::getCreatedOn))
                .get()
                .getCreatedOn();

        var uriAndId = events.stream()
                .collect(Collectors.toMap(e -> "/events/" + e.getId(), Event::getId));
        return statsClient.get(
                        startView,
                        LocalDateTime.now(),
                        new ArrayList<>(uriAndId.keySet()),
                        true).stream()
                .collect(Collectors.toMap(v -> uriAndId.get(v.getUri()), ViewStatsDto::getHits));
    }
}