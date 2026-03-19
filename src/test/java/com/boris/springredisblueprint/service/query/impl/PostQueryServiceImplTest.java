package com.boris.springredisblueprint.service.query.impl;

import com.boris.springredisblueprint.exception.PostNotFoundException;
import com.boris.springredisblueprint.mapper.PostMapper;
import com.boris.springredisblueprint.model.dto.PostDto;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.model.entity.User;
import com.boris.springredisblueprint.model.type.PostStatusEnum;
import com.boris.springredisblueprint.repository.PostRepository;
import com.boris.springredisblueprint.service.query.CategoryQueryService;
import com.boris.springredisblueprint.service.query.TagQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostQueryServiceImpl")
class PostQueryServiceImplTest {

    @InjectMocks
    private PostQueryServiceImpl postQueryService;

    @Mock
    private CategoryQueryService categoryQueryService;

    @Mock
    private TagQueryService tagQueryService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Nested
    @DisplayName("getAllPosts")
    class GetAllPosts {

        @Test
        @DisplayName("should query by status only when categoryId and tagId are null")
        void shouldQueryByStatusOnlyWhenNoFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Post post = buildPost();
            PostDto dto = buildPostDto();
            Page<Post> page = new PageImpl<>(List.of(post));

            when(postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, pageable)).thenReturn(page);
            when(postMapper.toDto(post)).thenReturn(dto);

            Page<PostDto> result = postQueryService.getAllPosts(null, null, pageable);

            assertThat(result.getContent()).containsExactly(dto);
            verify(postRepository).findAllByStatus(PostStatusEnum.PUBLISHED, pageable);
            verify(postRepository, never()).findAllByStatusAndCategory(any(), any(), any());
            verify(postRepository, never()).findAllByStatusAndTagsContaining(any(), any(), any());
            verify(postRepository, never()).findAllByStatusAndCategoryAndTagsContaining(any(), any(), any(), any());
        }

        @Test
        @DisplayName("should query by status and category when only categoryId is provided")
        void shouldQueryByCategoryWhenOnlyCategoryIdProvided() {
            UUID categoryId = UUID.randomUUID();
            Category category = buildCategory(categoryId);
            Pageable pageable = PageRequest.of(0, 10);
            Post post = buildPost();
            PostDto dto = buildPostDto();
            Page<Post> page = new PageImpl<>(List.of(post));

            when(categoryQueryService.getCategoryById(categoryId)).thenReturn(category);
            when(postRepository.findAllByStatusAndCategory(PostStatusEnum.PUBLISHED, category, pageable)).thenReturn(page);
            when(postMapper.toDto(post)).thenReturn(dto);

            Page<PostDto> result = postQueryService.getAllPosts(categoryId, null, pageable);

            assertThat(result.getContent()).containsExactly(dto);
            verify(postRepository).findAllByStatusAndCategory(PostStatusEnum.PUBLISHED, category, pageable);
            verify(postRepository, never()).findAllByStatus(any(), any());
        }

        @Test
        @DisplayName("should query by status and tag when only tagId is provided")
        void shouldQueryByTagWhenOnlyTagIdProvided() {
            UUID tagId = UUID.randomUUID();
            Tag tag = buildTag(tagId);
            Pageable pageable = PageRequest.of(0, 10);
            Post post = buildPost();
            PostDto dto = buildPostDto();
            Page<Post> page = new PageImpl<>(List.of(post));

            when(tagQueryService.getTagById(tagId)).thenReturn(tag);
            when(postRepository.findAllByStatusAndTagsContaining(PostStatusEnum.PUBLISHED, tag, pageable)).thenReturn(page);
            when(postMapper.toDto(post)).thenReturn(dto);

            Page<PostDto> result = postQueryService.getAllPosts(null, tagId, pageable);

            assertThat(result.getContent()).containsExactly(dto);
            verify(postRepository).findAllByStatusAndTagsContaining(PostStatusEnum.PUBLISHED, tag, pageable);
            verify(postRepository, never()).findAllByStatus(any(), any());
        }

        @Test
        @DisplayName("should query by status, category and tag when both categoryId and tagId are provided")
        void shouldQueryByCategoryAndTagWhenBothProvided() {
            UUID categoryId = UUID.randomUUID();
            UUID tagId = UUID.randomUUID();
            Category category = buildCategory(categoryId);
            Tag tag = buildTag(tagId);
            Pageable pageable = PageRequest.of(0, 10);
            Post post = buildPost();
            PostDto dto = buildPostDto();
            Page<Post> page = new PageImpl<>(List.of(post));

            when(categoryQueryService.getCategoryById(categoryId)).thenReturn(category);
            when(tagQueryService.getTagById(tagId)).thenReturn(tag);
            when(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatusEnum.PUBLISHED, category, tag, pageable)).thenReturn(page);
            when(postMapper.toDto(post)).thenReturn(dto);

            Page<PostDto> result = postQueryService.getAllPosts(categoryId, tagId, pageable);

            assertThat(result.getContent()).containsExactly(dto);
            verify(postRepository).findAllByStatusAndCategoryAndTagsContaining(PostStatusEnum.PUBLISHED, category, tag, pageable);
            verify(postRepository, never()).findAllByStatus(any(), any());
        }

        @Test
        @DisplayName("should return empty page when no posts match")
        void shouldReturnEmptyPageWhenNoPostsMatch() {
            Pageable pageable = PageRequest.of(0, 10);
            when(postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<PostDto> result = postQueryService.getAllPosts(null, null, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(postMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("should map each post to a DTO via PostMapper")
        void shouldMapPostsToDtos() {
            Pageable pageable = PageRequest.of(0, 10);
            Post post1 = buildPost();
            Post post2 = buildPost();
            PostDto dto1 = buildPostDto();
            PostDto dto2 = buildPostDto();

            when(postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, pageable))
                    .thenReturn(new PageImpl<>(List.of(post1, post2)));
            when(postMapper.toDto(post1)).thenReturn(dto1);
            when(postMapper.toDto(post2)).thenReturn(dto2);

            Page<PostDto> result = postQueryService.getAllPosts(null, null, pageable);

            assertThat(result.getContent()).containsExactly(dto1, dto2);
            verify(postMapper).toDto(post1);
            verify(postMapper).toDto(post2);
        }
    }

    @Nested
    @DisplayName("getPost")
    class GetPost {

        @Test
        @DisplayName("should return mapped PostDto when post exists")
        void shouldReturnPostDtoWhenFound() {
            UUID id = UUID.randomUUID();
            Post post = buildPost();
            PostDto dto = buildPostDto();

            when(postRepository.findById(id)).thenReturn(Optional.of(post));
            when(postMapper.toDto(post)).thenReturn(dto);

            PostDto result = postQueryService.getPost(id);

            assertThat(result).isEqualTo(dto);
        }

        @Test
        @DisplayName("should throw PostNotFoundException when post does not exist")
        void shouldThrowWhenPostNotFound() {
            UUID id = UUID.randomUUID();
            when(postRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postQueryService.getPost(id))
                    .isInstanceOf(PostNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    @DisplayName("getDraftPosts")
    class GetDraftPosts {

        @Test
        @DisplayName("should return paginated draft posts for the given user")
        void shouldReturnDraftPostsForUser() {
            User user = buildUser();
            Pageable pageable = PageRequest.of(0, 10);
            Post post = buildPost();
            PostDto dto = buildPostDto();
            Page<Post> page = new PageImpl<>(List.of(post));

            when(postRepository.findAllByAuthorAndStatus(user, PostStatusEnum.DRAFT, pageable)).thenReturn(page);
            when(postMapper.toDto(post)).thenReturn(dto);

            Page<PostDto> result = postQueryService.getDraftPosts(user, pageable);

            assertThat(result.getContent()).containsExactly(dto);
            verify(postRepository).findAllByAuthorAndStatus(user, PostStatusEnum.DRAFT, pageable);
        }

        @Test
        @DisplayName("should return empty page when user has no drafts")
        void shouldReturnEmptyPageWhenNoDrafts() {
            User user = buildUser();
            Pageable pageable = PageRequest.of(0, 10);

            when(postRepository.findAllByAuthorAndStatus(user, PostStatusEnum.DRAFT, pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<PostDto> result = postQueryService.getDraftPosts(user, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(postMapper, never()).toDto(any());
        }
    }

    private Post buildPost() {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setTitle("Test Post");
        return post;
    }

    private PostDto buildPostDto() {
        PostDto dto = new PostDto();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Test Post");
        return dto;
    }

    private Category buildCategory(UUID id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }

    private Tag buildTag(UUID id) {
        Tag tag = new Tag();
        tag.setId(id);
        return tag;
    }

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }
}