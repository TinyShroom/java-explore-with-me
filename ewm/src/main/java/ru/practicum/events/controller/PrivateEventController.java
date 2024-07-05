package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.events.dto.EventCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventRequestStatusUpdateDto;
import ru.practicum.events.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventUserUpdateDto;
import ru.practicum.events.service.PrivateEventService;
import ru.practicum.logging.Logging;
import ru.practicum.requests.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final PrivateEventService privateEventService;

    @Logging
    @GetMapping
    public List<EventShortDto> getAll(@PathVariable long userId,
                                      @RequestParam(defaultValue = "0") @Min(0) int from,
                                      @RequestParam(defaultValue = "10") @Min(1) int size) {
        var pageable = PageRequest.of(from / size, size, Sort.unsorted());
        return privateEventService.getAll(userId, pageable);
    }

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable long userId,
                               @RequestBody @Valid EventCreateDto eventCreateDto) {
        return privateEventService.create(userId, eventCreateDto);
    }

    @Logging
    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable long userId, @PathVariable long eventId) {
        return privateEventService.get(userId, eventId);
    }

    @Logging
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable long userId, @PathVariable long eventId,
                               @RequestBody @Valid EventUserUpdateDto updateEventDto) {
        return privateEventService.update(userId, eventId, updateEventDto);
    }

    @Logging
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId, @PathVariable long eventId) {
        return privateEventService.getRequests(userId, eventId);
    }

    @Logging
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateRequest(@PathVariable long userId, @PathVariable long eventId,
                                      @RequestBody @Valid EventRequestStatusUpdateDto statusUpdateRequest) {
        return privateEventService.updateRequest(userId, eventId, statusUpdateRequest);
    }
}