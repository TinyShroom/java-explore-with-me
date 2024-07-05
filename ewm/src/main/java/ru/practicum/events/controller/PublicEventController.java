package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventPublicSortBy;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.service.PublicEventService;
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
public class PublicEventController {

    private final PublicEventService publicEventService;
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
            @RequestParam(required = false) EventPublicSortBy sort,
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
        if (sort == EventPublicSortBy.EVENT_DATE) {
            sortBy = Sort.by("eventDate");
        }
        var pageable = PageRequest.of(from / size, size, sortBy);
        var result = publicEventService.getAll(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, pageable);
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
        var result = publicEventService.get(eventId);
        statsClient.post(EndpointHitCreateDto.builder()
                .app(APP_NAME)
                .ip(servletRequest.getRemoteAddr())
                .uri(servletRequest.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
        return result;
    }
}