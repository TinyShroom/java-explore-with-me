package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventAdminGetParameter;
import ru.practicum.events.dto.EventAdminUpdateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.model.EventState;
import ru.practicum.events.service.EventService;
import ru.practicum.logging.Logging;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;

    @Logging
    @GetMapping
    public List<EventFullDto> get(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        var pageRequest = PageRequest.of(from / size, size, Sort.unsorted());
        return eventService.getAllFull(EventAdminGetParameter.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build(), pageRequest);
    }

    @Logging
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable long eventId, @RequestBody @Valid EventAdminUpdateDto eventAdminUpdateDto) {
        return eventService.update(eventId, eventAdminUpdateDto);
    }
}
