package ru.practicum.compilations.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class CompilationUpdateDto {
    private List<Long> events;
    private Boolean pinned;
    @Size(max = 50)
    private String title;
}
