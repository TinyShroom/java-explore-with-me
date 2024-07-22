package ru.practicum.events.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventPublicGetParameter {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private boolean onlyAvailable;
    private EventPublicSort sort;
}
