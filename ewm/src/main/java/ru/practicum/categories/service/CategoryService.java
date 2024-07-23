package ru.practicum.categories.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.categories.dto.CategoryCreateDto;
import ru.practicum.categories.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CategoryCreateDto user);

    void delete(long catId);

    CategoryDto update(long catId, CategoryCreateDto newCategory);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto get(long catId);
}