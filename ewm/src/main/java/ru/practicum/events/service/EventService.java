package ru.practicum.events.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.events.dto.*;

import java.util.List;

public interface EventService {

    List<EventFullDto> getAllFull(EventAdminGetParameter parameter, PageRequest pageRequest);

    EventFullDto update(long eventId, EventAdminUpdateDto eventAdminUpdateDto);

    List<EventShortDto> getAllShort(long userId, Pageable pageable);

    EventFullDto create(long userId, EventCreateDto eventCreateDto);

    EventFullDto get(long userId, long eventId);

    EventFullDto update(long userId, long eventId, EventUserUpdateDto updateEventDto);

    List<EventShortDto> getAllShort(EventPublicGetParameter parameter, Pageable pageable);

    EventFullDto get(long eventId);
}