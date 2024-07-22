package ru.practicum.categories.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.service.CategoryService;
import ru.practicum.logging.Logging;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class CategoriesPublicController {

    private final CategoryService categoryService;

    @Logging
    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") @Min(0) int from,
                                    @RequestParam(defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        return categoryService.getAll(pageable);
    }

    @Logging
    @GetMapping("/{catId}")
    public CategoryDto get(@PathVariable long catId) {
        return categoryService.get(catId);
    }
}