package com.boris.springredisblueprint.service.impl;

import com.boris.springredisblueprint.exception.CategoryNotFoundException;
import com.boris.springredisblueprint.model.entities.Category;
import com.boris.springredisblueprint.repository.CategoryRepository;
import com.boris.springredisblueprint.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new IllegalArgumentException("Category already exists with name: " + category.getName());
        }

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> listCategories() {
        return categoryRepository.findAllWithPostCount();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new CategoryNotFoundException(String.format("Post with ID '%s' not found.", id)));
    }

    @Override
    public void deleteCategory(UUID id) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            if (!category.get().getPosts().isEmpty()) {
                throw new IllegalStateException("Category has associated posts.");
            }

            categoryRepository.deleteById(id);
        }
    }
}
