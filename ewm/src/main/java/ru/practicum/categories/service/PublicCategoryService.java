package ru.practicum.categories.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.categories.dto.CategoryDto;

import java.util.List;

public interface PublicCategoryService {

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto get(long catId);
}