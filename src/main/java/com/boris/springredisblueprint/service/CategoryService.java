package com.boris.springredisblueprint.service;

import com.boris.springredisblueprint.model.entities.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<Category> listCategories();

    Category createCategory(Category category);

    void deleteCategory(UUID id);

    Category getCategoryById(UUID categoryId);
}
