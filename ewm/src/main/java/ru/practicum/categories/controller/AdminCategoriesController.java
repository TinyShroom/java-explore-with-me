package ru.practicum.categories.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryCreateDto;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.service.AdminCategoryService;
import ru.practicum.logging.Logging;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class AdminCategoriesController {

    private final AdminCategoryService adminCategoryService;

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CategoryCreateDto categoryCreateDto) {
        return adminCategoryService.create(categoryCreateDto);
    }

    @Logging
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long catId) {
        adminCategoryService.delete(catId);
    }

    @Logging
    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable long catId, @RequestBody @Valid CategoryCreateDto categoryCreateDto) {
        return adminCategoryService.update(catId, categoryCreateDto);
    }
}
