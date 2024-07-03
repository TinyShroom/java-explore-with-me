package ru.practicum.mapper;

import ru.practicum.dto.EndpointHitCreateDto;
import ru.practicum.model.EndpointHit;

public class EndpointHitMapper {

    public static EndpointHit toModel(EndpointHitCreateDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .ip(dto.getIp())
                .uri(dto.getUri())
                .timestamp(dto.getTimestamp())
                .build();
    }
}
