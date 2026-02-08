package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.model.dto.CategoryDto;
import com.boris.springredisblueprint.model.dto.CreateCategoryRequestDto;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.mapper.CategoryMapper;
import com.boris.springredisblueprint.service.command.CategoryCommandService;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryQueryService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        Category categoryToCreate = categoryMapper.toEntity(createCategoryRequestDto);
        Category savedCategory = categoryCommandService.createCategory(categoryToCreate);

        return new ResponseEntity<>(
                categoryMapper.toDTO(savedCategory),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryCommandService.deleteCategory(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
