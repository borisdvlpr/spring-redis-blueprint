package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.model.dto.PostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PostController")
class PostControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/posts";

    @Nested
    @DisplayName("GET /api/v1/posts")
    class GetAllPosts {

        @Test
        @DisplayName("should return 200 with empty page when no posts exist")
        void shouldReturn200WithEmptyPage() throws Exception {
            when(postQueryService.getAllPosts(isNull(), isNull(), any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(postQueryService).getAllPosts(isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should return 200 with paginated posts")
        void shouldReturn200WithPosts() throws Exception {
            PostDto post1 = buildPostDto("First Post");
            PostDto post2 = buildPostDto("Second Post");
            List<PostDto> posts = List.of(post1, post2);

            when(postQueryService.getAllPosts(isNull(), isNull(), any()))
                    .thenReturn(new PageImpl<>(posts, PageRequest.of(0, 20), posts.size()));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].title").value("First Post"))
                    .andExpect(jsonPath("$.content[1].title").value("Second Post"))
                    .andExpect(jsonPath("$.totalElements").value(2));

            verify(postQueryService).getAllPosts(isNull(), isNull(), any());
        }

        @Test
        @DisplayName("should filter posts by categoryId when provided")
        void shouldFilterByCategoryId() throws Exception {
            UUID categoryId = UUID.randomUUID();
            PostDto post = buildPostDto("Categorized Post");

            when(postQueryService.getAllPosts(eq(categoryId), isNull(), any()))
                    .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1));

            mockMvc.perform(get(BASE_URL).param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Categorized Post"));

            verify(postQueryService).getAllPosts(eq(categoryId), isNull(), any());
        }

        @Test
        @DisplayName("should filter posts by tagId when provided")
        void shouldFilterByTagId() throws Exception {
            UUID tagId = UUID.randomUUID();
            PostDto post = buildPostDto("Tagged Post");

            when(postQueryService.getAllPosts(isNull(), eq(tagId), any()))
                    .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1));

            mockMvc.perform(get(BASE_URL).param("tagId", tagId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Tagged Post"));

            verify(postQueryService).getAllPosts(isNull(), eq(tagId), any());
        }

        @Test
        @DisplayName("should filter posts by both categoryId and tagId when provided")
        void shouldFilterByCategoryIdAndTagId() throws Exception {
            UUID categoryId = UUID.randomUUID();
            UUID tagId = UUID.randomUUID();
            PostDto post = buildPostDto("Filtered Post");

            when(postQueryService.getAllPosts(eq(categoryId), eq(tagId), any()))
                    .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1));

            mockMvc.perform(get(BASE_URL)
                            .param("categoryId", categoryId.toString())
                            .param("tagId", tagId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Filtered Post"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(postQueryService).getAllPosts(eq(categoryId), eq(tagId), any());
        }

        @Test
        @DisplayName("should respect pagination parameters")
        void shouldRespectPaginationParams() throws Exception {
            Pageable expectedPageable = PageRequest.of(2, 5);

            when(postQueryService.getAllPosts(isNull(), isNull(), eq(expectedPageable)))
                    .thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));

            mockMvc.perform(get(BASE_URL)
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value(2))
                    .andExpect(jsonPath("$.size").value(5));

            verify(postQueryService).getAllPosts(isNull(), isNull(), eq(expectedPageable));
        }

        @Test
        @DisplayName("should return 400 when categoryId is not a valid UUID")
        void shouldReturn400WhenCategoryIdIsInvalid() throws Exception {
            mockMvc.perform(get(BASE_URL).param("categoryId", "not-a-uuid"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(postQueryService);
        }

        @Test
        @DisplayName("should return 400 when tagId is not a valid UUID")
        void shouldReturn400WhenTagIdIsInvalid() throws Exception {
            mockMvc.perform(get(BASE_URL).param("tagId", "not-a-uuid"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(postQueryService);
        }
    }

    private PostDto buildPostDto(String title) {
        return PostDto.builder()
                .id(UUID.randomUUID())
                .title(title)
                .build();
    }
}
