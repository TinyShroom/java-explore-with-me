package ru.practicum.compilations.dto;

import lombok.Data;
import ru.practicum.events.dto.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {
    private long id;
    private List<EventShortDto> events;
    private boolean pinned;
    private String title;
}
