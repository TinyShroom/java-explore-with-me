package ru.practicum;

import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {

    List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

    void post(EndpointHitCreateDto endpointHitCreateDto);
}