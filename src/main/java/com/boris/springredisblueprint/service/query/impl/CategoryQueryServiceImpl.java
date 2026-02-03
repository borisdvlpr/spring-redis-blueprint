package com.boris.springredisblueprint.service.query.impl;

import com.boris.springredisblueprint.exception.CategoryNotFoundException;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.repository.CategoryRepository;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> listCategories() {
        return categoryRepository.findAllWithPostCount();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new CategoryNotFoundException(String.format("Category with ID '%s' not found.", id)));
    }
}
