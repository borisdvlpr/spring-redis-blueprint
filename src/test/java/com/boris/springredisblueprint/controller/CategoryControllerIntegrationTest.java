package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.model.dto.CategoryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CategoryController")
class CategoryControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/categories";

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetAllCategories {

        @Test
        @DisplayName("should return 200 with empty list when no categories exist")
        void shouldReturn200WithEmptyList() throws Exception {
            when(categoryQueryService.getAllCategories()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(categoryQueryService).getAllCategories();
        }

        @Test
        @DisplayName("should return 200 with all categories")
        void shouldReturn200WithCategories() throws Exception {
            CategoryDto category1 = buildCategoryDto("Technology");
            CategoryDto category2 = buildCategoryDto("Science");

            when(categoryQueryService.getAllCategories()).thenReturn(List.of(category1, category2));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Technology"))
                    .andExpect(jsonPath("$[1].name").value("Science"));

            verify(categoryQueryService).getAllCategories();
        }

        @Test
        @DisplayName("should return 200 with a single category")
        void shouldReturn200WithSingleCategory() throws Exception {
            CategoryDto category = buildCategoryDto("Sports");
            when(categoryQueryService.getAllCategories()).thenReturn(List.of(category));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Sports"))
                    .andExpect(jsonPath("$[0].id").value(category.getId().toString()));

            verify(categoryQueryService).getAllCategories();
        }
    }

    private CategoryDto buildCategoryDto(String name) {
        return CategoryDto.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();
    }
}
