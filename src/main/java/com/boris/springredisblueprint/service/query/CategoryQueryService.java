package com.boris.springredisblueprint.service.query;

import com.boris.springredisblueprint.model.dto.CategoryDto;
import com.boris.springredisblueprint.model.entity.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryQueryService {
    List<CategoryDto> getAllCategories();

    Category getCategoryById(UUID categoryId);
}
