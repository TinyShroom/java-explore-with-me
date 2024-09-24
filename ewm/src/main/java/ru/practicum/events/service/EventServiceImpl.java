package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.categories.dao.CategoryRepository;
import ru.practicum.categories.model.Category;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.events.dao.EventRepository;
import ru.practicum.events.dto.EventAdminGetParameter;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventPrivateGetParameter;
import ru.practicum.events.dto.EventPublicGetParameter;
import ru.practicum.events.dto.EventPublicSort;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUserUpdateDto;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;
import ru.practicum.events.model.RequestsCount;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dao.ParticipationRequestRepository;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.users.dao.UserRepository;
import ru.practicum.users.model.User;

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
public class EventServiceImpl implements EventService {

    private final ParticipationRequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllFull(EventAdminGetParameter parameter, PageRequest pageRequest) {
        var specification = new Specification<Event>() {
            @Override
            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (parameter.getUsers() != null && !parameter.getUsers().isEmpty()) {
                    predicates.add(root.get("initiator").in(parameter.getUsers()));
                }
                if (parameter.getStates() != null && !parameter.getStates().isEmpty()) {
                    predicates.add(root.get("state").in(parameter.getStates()));
                }
                if (parameter.getCategories() != null && !parameter.getCategories().isEmpty()) {
                    predicates.add(root.get("category").in(parameter.getCategories()));
                }
                if (parameter.getRangeStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"),
                            parameter.getRangeStart()));
                }
                if (parameter.getRangeEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), parameter.getRangeEnd()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        var events = eventRepository.findAll(specification, pageRequest).stream()
                .collect(Collectors.toList());
        if (events.isEmpty()) return Collections.emptyList();

        var confirmedRequests = getConfirmedRequests(events);
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

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllShort(long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var events = eventRepository.findAllByInitiatorId(userId, pageable);
        var confirmedRequests = getConfirmedRequests(events);
        var viewStats = getViews(events);
        return events.stream()
                .map(e -> eventMapper.toShortDto(e, viewStats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public EventFullDto get(long userId, long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        var event = eventRepository.findByIdAndInitiatorId(eventId, userId)
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

    @Override
    @Transactional
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
    public List<EventShortDto> getAllShort(EventPublicGetParameter parameter, Pageable pageable) {
        var specification = new Specification<Event>() {
            @Override
            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (parameter.getText() != null) {
                    var textLike = parameter.getText().toLowerCase();
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), textLike),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), textLike))
                    );
                }
                if (parameter.getCategories() != null && !parameter.getCategories().isEmpty()) {
                    predicates.add(root.get("category").in(parameter.getCategories()));
                }
                if (parameter.getPaid() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("paid"), parameter.getPaid()));
                }
                if (parameter.getRangeStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"),
                            parameter.getRangeStart()));
                }
                if (parameter.getRangeEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), parameter.getRangeEnd()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        var events = eventRepository.findAll(specification, pageable).stream()
                .collect(Collectors.toList());
        List<EventShortDto> result = Collections.emptyList();
        if (events.isEmpty()) return result;

        var confirmedRequests = getConfirmedRequests(events);

        if (parameter.isOnlyAvailable()) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() <= 0 ||
                            e.getParticipantLimit() > confirmedRequests.getOrDefault(e.getId(), 0L))
                    .collect(Collectors.toList());
        }
        var viewStats = getViews(events);
        result = events.stream()
                .map(e -> eventMapper.toShortDto(e, viewStats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
        if (parameter.getSort() == EventPublicSort.VIEWS) {
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

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllSubscriptionsShort(EventPrivateGetParameter parameter, Pageable pageable) {
        var userId = parameter.getUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
        if (user.getSubscriptions().isEmpty()) {
            return Collections.emptyList();
        }
        var subscriptionIds = user.getSubscriptions().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        var specification = new Specification<Event>() {
            @Override
            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(root.get("initiator").in(subscriptionIds));
                if (parameter.getCategories() != null && !parameter.getCategories().isEmpty()) {
                    predicates.add(root.get("category").in(parameter.getCategories()));
                }
                if (parameter.getPaid() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("paid"), parameter.getPaid()));
                }
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), parameter.getRangeStart()));

                if (parameter.getRangeEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), parameter.getRangeEnd()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        var events = eventRepository.findAll(specification, pageable).stream()
                .collect(Collectors.toList());
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        var confirmedRequests = getConfirmedRequests(events);
        if (parameter.isOnlyAvailable()) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() <= 0 ||
                            e.getParticipantLimit() > confirmedRequests.getOrDefault(e.getId(), 0L))
                    .collect(Collectors.toList());
        }
        var viewStats = getViews(events);
        return events.stream()
                .map(e -> eventMapper.toShortDto(e, viewStats.getOrDefault(e.getId(), 0L),
                        confirmedRequests.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getConfirmedRequests(final List<Event> events) {
        var eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        return requestRepository.countAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(RequestsCount::getEventId, RequestsCount::getCount));
    }

    private Map<Long, Long> getViews(final List<Event> events) {
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