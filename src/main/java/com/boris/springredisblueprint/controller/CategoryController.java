package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.mapper.CategoryMapper;
import com.boris.springredisblueprint.model.dto.CategoryDto;
import com.boris.springredisblueprint.model.dto.CreateCategoryRequestDto;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.service.command.CategoryCommandService;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping(path = "/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.info("GET /api/v1/categories");
        List<CategoryDto> categories = categoryQueryService.getAllCategories();

        log.debug("Returning {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        log.info("POST /api/v1/categories - category: '{}'", createCategoryRequestDto.getName());

        Category categoryToCreate = categoryMapper.toEntity(createCategoryRequestDto);
        Category savedCategory = categoryCommandService.createCategory(categoryToCreate);

        log.debug("Returning saved category - name: '{}', id: '{}'",
                savedCategory.getName(), savedCategory.getId());

        return new ResponseEntity<>(
                categoryMapper.toDTO(savedCategory),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        log.info("DELETE /api/v1/categories/{}", id);
        categoryCommandService.deleteCategory(id);

        log.debug("Deleted category - id: '{}'", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
