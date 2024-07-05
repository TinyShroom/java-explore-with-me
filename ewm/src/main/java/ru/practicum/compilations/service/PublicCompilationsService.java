package ru.practicum.compilations.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.compilations.dto.CompilationDto;

import java.util.List;

public interface PublicCompilationsService {

    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    CompilationDto get(long compId);
}