package ru.practicum.events.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {

    List<EventFullDto> get(List<Long> users,
                           List<EventState> states,
                           List<Long> categories,
                           LocalDateTime rangeStart,
                           LocalDateTime rangeEnd,
                           Pageable pageRequest);

    EventFullDto update(long eventId, EventAdminUpdateDto eventAdminUpdateDto);
}