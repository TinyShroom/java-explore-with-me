package ru.practicum.events.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventPublicSortBy;
import ru.practicum.events.dto.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getAll(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                               LocalDateTime rangeEnd, boolean onlyAvailable, EventPublicSortBy sort, Pageable pageable);

    EventFullDto get(long eventId);
}