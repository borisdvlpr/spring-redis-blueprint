package com.boris.springredisblueprint.service.query;

import com.boris.springredisblueprint.model.entity.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryQueryService {
    List<Category> listCategories();

    Category getCategoryById(UUID categoryId);
}
