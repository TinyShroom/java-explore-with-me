package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventPublicGetParameter;
import ru.practicum.events.dto.EventPublicSort;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.service.EventService;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.logging.Logging;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;
    private final StatsClient statsClient;

    private static final String APP_NAME = "ewm-main-service";

    @Logging
    @GetMapping
    public List<EventShortDto> getAll(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventPublicSort sort,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            HttpServletRequest servletRequest
    ) {
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        } else if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException(ErrorMessages.START_AFTER_END.getFormatMessage(
                    rangeStart.toString(), rangeEnd.toString()));
        }
        var sortBy = Sort.unsorted();
        if (sort == EventPublicSort.EVENT_DATE) {
            sortBy = Sort.by("eventDate");
        }
        var pageable = PageRequest.of(from / size, size, sortBy);
        var result = eventService.getAllShort(EventPublicGetParameter.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .build(), pageable);
        statsClient.post(EndpointHitCreateDto.builder()
                .app(APP_NAME)
                .ip(servletRequest.getRemoteAddr())
                .uri(servletRequest.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
        return result;
    }

    @Logging
    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable long eventId, HttpServletRequest servletRequest) {
        var result = eventService.get(eventId);
        statsClient.post(EndpointHitCreateDto.builder()
                .app(APP_NAME)
                .ip(servletRequest.getRemoteAddr())
                .uri(servletRequest.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
        return result;
    }
}