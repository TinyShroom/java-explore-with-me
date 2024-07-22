package ru.practicum.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dao.CategoryRepository;
import ru.practicum.categories.dto.CategoryCreateDto;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.exception.ErrorMessages;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto categoryCreateDto) {
        var result = categoryRepository.save(categoryMapper.toModel(categoryCreateDto));
        return categoryMapper.toDto(result);
    }

    @Override
    @Transactional
    public void delete(long catId) {
        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(catId)));
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto update(long catId, CategoryCreateDto newCategory) {
        var oldCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(catId)));
        oldCategory.setName(newCategory.getName());
        return categoryMapper.toDto(categoryRepository.save(oldCategory));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(Pageable pageable) {
        return categoryMapper.toDto(categoryRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto get(long catId) {
        var category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getFormatMessage(catId)));
        return categoryMapper.toDto(category);
    }
}