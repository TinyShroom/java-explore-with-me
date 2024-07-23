package ru.practicum.compilations.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class CompilationCreateDto {
    private Set<Long> events;
    private boolean pinned;
    @NotBlank
    @Size(max = 50)
    private String title;
}