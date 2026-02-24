package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.mapper.CategoryMapper;
import com.boris.springredisblueprint.model.dto.ApiErrorResponse;
import com.boris.springredisblueprint.model.dto.CategoryDto;
import com.boris.springredisblueprint.model.dto.CreateCategoryRequestDto;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.service.command.CategoryCommandService;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Categories", description = "Manage post categories")
public class CategoryController {
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper;

    @Operation(summary = "Get all categories", description = "Returns a list of all available categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.info("GET /api/v1/categories");
        List<CategoryDto> categories = categoryQueryService.getAllCategories();

        log.debug("Returning {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Create a category", description = "Creates a new category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
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

    @Operation(summary = "Delete a category", description = "Deletes a category by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "UUID of the category to delete") @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/categories/{}", id);
        categoryCommandService.deleteCategory(id);

        log.debug("Deleted category - id: '{}'", id);
        return ResponseEntity.noContent().build();
    }
}
