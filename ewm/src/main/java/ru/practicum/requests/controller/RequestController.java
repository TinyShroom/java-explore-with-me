package ru.practicum.requests.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.logging.Logging;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @Logging
    @GetMapping
    public List<ParticipationRequestDto> get(@PathVariable long userId) {
        return requestService.getAll(userId);
    }

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable long userId, @RequestParam long eventId) {
        return requestService.create(userId, eventId);
    }

    @Logging
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto update(@PathVariable long userId, @PathVariable long requestId) {
        return requestService.update(userId, requestId);
    }
}
