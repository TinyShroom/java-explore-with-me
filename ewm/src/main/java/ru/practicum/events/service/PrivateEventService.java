package ru.practicum.events.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventRequestStatusUpdateDto;
import ru.practicum.events.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUserUpdateDto;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {

    List<EventShortDto> getAll(long userId, Pageable pageable);

    EventFullDto create(long userId, EventCreateDto eventCreateDto);

    EventFullDto get(long userId, long eventId);

    EventFullDto update(long userId, long eventId, EventUserUpdateDto updateEventDto);

    List<ParticipationRequestDto> getRequests(long userId, long eventId);

    EventRequestStatusUpdateResultDto updateRequest(long userId, long eventId, EventRequestStatusUpdateDto statusUpdateRequest);
}