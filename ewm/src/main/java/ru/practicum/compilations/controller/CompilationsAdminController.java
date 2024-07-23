package ru.practicum.compilations.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationCreateDto;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.CompilationUpdateDto;
import ru.practicum.compilations.service.CompilationsService;
import ru.practicum.logging.Logging;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class CompilationsAdminController {

    private final CompilationsService compilationsService;

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid CompilationCreateDto compilationCreateDto) {
        return compilationsService.create(compilationCreateDto);
    }

    @Logging
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long compId) {
        compilationsService.delete(compId);
    }

    @Logging
    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable long compId, @RequestBody @Valid CompilationUpdateDto compilationUpdateDto) {
        return compilationsService.update(compId, compilationUpdateDto);
    }
}
