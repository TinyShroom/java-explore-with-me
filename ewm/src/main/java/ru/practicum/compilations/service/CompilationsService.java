package ru.practicum.compilations.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.compilations.dto.CompilationCreateDto;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.CompilationUpdateDto;

import java.util.List;

public interface CompilationsService {

    CompilationDto create(CompilationCreateDto user);

    void delete(long compId);

    CompilationDto update(long compId, CompilationUpdateDto newCategory);

    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    CompilationDto get(long compId);
}