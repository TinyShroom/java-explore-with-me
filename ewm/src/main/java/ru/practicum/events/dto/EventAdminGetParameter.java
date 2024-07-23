package ru.practicum.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.events.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventAdminGetParameter {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
}
