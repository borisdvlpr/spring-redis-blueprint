package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.model.dto.TagDto;
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

@DisplayName("TagController")
class TagControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/tags";

    @Nested
    @DisplayName("GET /api/v1/tags")
    class GetAllTags {

        @Test
        @DisplayName("should return 200 with empty list when no tags exist")
        void shouldReturn200WithEmptyList() throws Exception {
            when(tagQueryService.getTags()).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(tagQueryService).getTags();
        }

        @Test
        @DisplayName("should return 200 with all tags")
        void shouldReturn200WithTags() throws Exception {
            TagDto tag1 = buildTagDto("java");
            TagDto tag2 = buildTagDto("spring");
            TagDto tag3 = buildTagDto("redis");

            when(tagQueryService.getTags()).thenReturn(List.of(tag1, tag2, tag3));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].name").value("java"))
                    .andExpect(jsonPath("$[1].name").value("spring"))
                    .andExpect(jsonPath("$[2].name").value("redis"));

            verify(tagQueryService).getTags();
        }

        @Test
        @DisplayName("should return 200 with a single tag")
        void shouldReturn200WithSingleTag() throws Exception {
            TagDto tag = buildTagDto("microservices");
            when(tagQueryService.getTags()).thenReturn(List.of(tag));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("microservices"))
                    .andExpect(jsonPath("$[0].id").value(tag.getId().toString()));

            verify(tagQueryService).getTags();
        }
    }

    // TODO: add @Nested classes for POST /api/v1/tags and DELETE /api/v1/tags/{id}

    private TagDto buildTagDto(String name) {
        return TagDto.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();
    }
}
