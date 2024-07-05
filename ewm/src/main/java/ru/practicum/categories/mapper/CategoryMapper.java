package ru.practicum.categories.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.practicum.categories.dto.CategoryCreateDto;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toModel(CategoryCreateDto categoryCreateDto);

    CategoryDto toDto(Category category);

    List<CategoryDto> toDto(Page<Category> categories);
}