package com.boris.springredisblueprint.service.command;

import com.boris.springredisblueprint.model.entity.Category;

import java.util.UUID;

public interface CategoryCommandService {
    Category createCategory(Category category);

    void deleteCategory(UUID id);
}
