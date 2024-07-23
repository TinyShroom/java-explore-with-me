package ru.practicum.compilations.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.service.CompilationsService;
import ru.practicum.logging.Logging;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class CompilationsPublicController {

    private final CompilationsService compilationsService;

    @Logging
    @GetMapping
    public List<CompilationDto> getAll(@RequestParam(required = false) Boolean pinned,
                                       @RequestParam(defaultValue = "0") @Min(0) int from,
                                       @RequestParam(defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.unsorted());
        return compilationsService.getAll(pinned, pageable);
    }

    @Logging
    @GetMapping("/{compId}")
    public CompilationDto get(@PathVariable long compId) {
        return compilationsService.get(compId);
    }
}