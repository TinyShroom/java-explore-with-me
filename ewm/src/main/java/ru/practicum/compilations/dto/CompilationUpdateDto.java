package ru.practicum.compilations.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class CompilationUpdateDto {
    private Set<Long> events;
    private Boolean pinned;
    @Size(max = 50)
    private String title;
}
