package ru.practicum.events.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @EntityGraph("events-short")
    List<Event> findAllByInitiatorId(long userId, Pageable pageable);

    @EntityGraph("events-full")
    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    @EntityGraph("events-full")
    Optional<Event> findByIdAndState(long eventId, EventState eventState);

    @EntityGraph("events-short")
    List<Event> findAllById(Iterable<Long> eventIds);

    @EntityGraph("events-full")
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
