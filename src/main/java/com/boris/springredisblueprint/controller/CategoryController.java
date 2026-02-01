package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.model.dto.CategoryDto;
import com.boris.springredisblueprint.model.dto.CreateCategoryRequestDto;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.mapper.CategoryMapper;
import com.boris.springredisblueprint.service.CategoryService;
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
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> listCategories() {
        List<CategoryDto> categories = categoryService.listCategories().stream()
                .map(categoryMapper::toDTO)
                .toList();

        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        Category categoryToCreate = categoryMapper.toEntity(createCategoryRequestDto);
        Category savedCategory = categoryService.createCategory(categoryToCreate);

        return new ResponseEntity<>(
                categoryMapper.toDTO(savedCategory),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
