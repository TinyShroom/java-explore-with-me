package ru.practicum.categories.service;

import ru.practicum.categories.dto.CategoryCreateDto;
import ru.practicum.categories.dto.CategoryDto;

public interface AdminCategoryService {

    CategoryDto create(CategoryCreateDto user);

    void delete(long catId);

    CategoryDto update(long catId, CategoryCreateDto newCategory);
}