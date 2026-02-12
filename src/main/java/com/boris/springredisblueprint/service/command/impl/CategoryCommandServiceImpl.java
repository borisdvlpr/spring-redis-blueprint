package com.boris.springredisblueprint.service.command.impl;

import com.boris.springredisblueprint.exception.CategoryNotFoundException;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.repository.CategoryRepository;
import com.boris.springredisblueprint.service.command.CategoryCommandService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryCommandServiceImpl implements CategoryCommandService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        log.info("Creating new category '{}'", category.getName());
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new IllegalArgumentException("Category already exists with name: " + category.getName());
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Successfully created category with id: '{}'", savedCategory.getId());

        return savedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category with id: '{}'", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category not found for deletion: {}", id);
                    return new CategoryNotFoundException(
                            String.format("Category with ID '%s' not found.", id));
                });

        if (!category.getPosts().isEmpty()) {
            throw new IllegalStateException("Category has associated posts.");
        }

        categoryRepository.delete(category);
        log.info("Successfully deleted category: '{}'", id);
    }
}
