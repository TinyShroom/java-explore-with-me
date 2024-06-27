package ru.practicum.service;

import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void create(EndpointHitCreateDto endpointHitCreateDto);

    List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
