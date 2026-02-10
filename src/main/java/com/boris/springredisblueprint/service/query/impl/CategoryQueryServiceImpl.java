package com.boris.springredisblueprint.service.query.impl;

import com.boris.springredisblueprint.exception.CategoryNotFoundException;
import com.boris.springredisblueprint.mapper.CategoryMapper;
import com.boris.springredisblueprint.model.dto.CategoryDto;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.repository.CategoryRepository;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getAllCategories() {
        List<CategoryDto> categories = categoryRepository.findAllWithPostCount()
                .stream()
                .map(categoryMapper::toDTO)
                .toList();

        log.info("Found {} categories", categories.size());

        return categories;
    }

    @Override
    public Category getCategoryById(UUID id) {
        log.info("Fetching category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with id: {}", id);
                    return new CategoryNotFoundException(
                            String.format("Category with ID '%s' not found.", id));
                });

        log.info("Successfully fetched category: {}", category.getName());

        return category;
    }
}
