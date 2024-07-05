package ru.practicum.compilations.service;

import ru.practicum.compilations.dto.CompilationCreateDto;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.CompilationUpdateDto;

public interface AdminCompilationsService {

    CompilationDto create(CompilationCreateDto user);

    void delete(long compId);

    CompilationDto update(long compId, CompilationUpdateDto newCategory);
}