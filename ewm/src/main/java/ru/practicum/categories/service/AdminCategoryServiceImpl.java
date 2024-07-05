package ru.practicum.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.categories.dao.CategoryRepository;
import ru.practicum.categories.dto.CategoryCreateDto;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto create(CategoryCreateDto categoryCreateDto) {
        var result = categoryRepository.save(categoryMapper.toModel(categoryCreateDto));
        return categoryMapper.toDto(result);
    }

    @Override
    public void delete(long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(catId)));
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto update(long catId, CategoryCreateDto newCategory) {
        var oldCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(catId)));
        oldCategory.setName(newCategory.getName());
        return categoryMapper.toDto(categoryRepository.save(oldCategory));
    }
}