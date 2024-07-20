package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.events.dao.EventRepository;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventPublicSortBy;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;
import ru.practicum.events.model.RequestsCount;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dao.ParticipationRequestRepository;
import ru.practicum.requests.model.RequestStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, boolean onlyAvailable, EventPublicSortBy sort,
                                      Pageable pageable) {
        var specification = new Specification<Event>() {
            @Override
            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (text != null) {
                    var textLike = text.toLowerCase();
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), textLike),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), textLike))
                    );
                }
                if (categories != null && !categories.isEmpty()) {
                    predicates.add(root.get("category").in(categories));
                }
                if (paid != null) {
                    predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
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
        var events = eventRepository.findAll(specification, pageable).stream()
                .collect(Collectors.toList());
        List<EventShortDto> result = Collections.emptyList();
        if (events.isEmpty()) return result;

        var confirmedRequests = requestRepository.countAllByEventIdInAndStatus(
                        events.stream()
                                .map(Event::getId)
                                .collect(Collectors.toList()),
                        RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(RequestsCount::getEventId, RequestsCount::getCount));

        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() <= 0 ||
                            e.getParticipantLimit() > confirmedRequests.getOrDefault(e.getId(), 0L))
                    .collect(Collectors.toList());
        }
        var startView = events.stream()
                .min(Comparator.comparing(Event::getCreatedOn))
                .get()
                .getCreatedOn();

        var uriAndId = events.stream()
                .collect(Collectors.toMap(e -> "/events/" + e.getId(), Event::getId));
        var viewStats = statsClient.get(
                        startView,
                        LocalDateTime.now(),
                        new ArrayList<>(uriAndId.keySet()),
                        true).stream()
                .collect(Collectors.toMap(v -> uriAndId.get(v.getUri()), ViewStatsDto::getHits));
        result = events.stream()
                .map(e -> eventMapper.toShortDto(e, viewStats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
        if (sort == EventPublicSortBy.VIEWS) {
            result.sort(Comparator.comparingLong(EventShortDto::getViews).reversed());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto get(long eventId) {
        var event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EVENT_NOT_FOUND.getFormatMessage(eventId)));
        var eventUri = "/events/" + event.getId();
        var viewStats = statsClient.get(
                        event.getCreatedOn(),
                        LocalDateTime.now(),
                        List.of(eventUri),
                        true);
        var confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        return eventMapper.toFullDto(event, viewStats.isEmpty() ? 0 : viewStats.get(0).getHits(), confirmedRequests);
    }
}