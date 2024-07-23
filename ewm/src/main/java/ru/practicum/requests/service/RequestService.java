package ru.practicum.requests.service;

import ru.practicum.events.dto.EventRequestStatusUpdateDto;
import ru.practicum.events.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getAll(long userId);

    ParticipationRequestDto create(long userId, long eventId);

    ParticipationRequestDto update(long userId, long requestId);

    List<ParticipationRequestDto> getRequests(long userId, long eventId);

    EventRequestStatusUpdateResultDto updateRequest(long userId, long eventId,
                                                    EventRequestStatusUpdateDto statusUpdateRequest);
}