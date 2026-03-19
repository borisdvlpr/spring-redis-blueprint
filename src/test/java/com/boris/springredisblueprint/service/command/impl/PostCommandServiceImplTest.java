package com.boris.springredisblueprint.service.command.impl;

import com.boris.springredisblueprint.exception.PostNotFoundException;
import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostCommandServiceImpl")
class PostCommandServiceImplTest {

    @InjectMocks
    private PostCommandServiceImpl postCommandService;
    @Mock
    private CategoryQueryService categoryQueryService;
    @Mock
    private TagQueryService tagQueryService;
    @Mock
    private PostRepository postRepository;

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("should create and return a post with all fields mapped")
        void shouldCreatePostWithAllFieldsMapped() {
            User user = buildUser();
            Category category = buildCategory();
            Tag tag = buildTag();
            CreatePostRequest request = buildCreatePostRequest("My Title", "word ".repeat(200), PostStatusEnum.PUBLISHED, category.getId(), Set.of(tag.getId()));

            when(categoryQueryService.getCategoryById(category.getId())).thenReturn(category);
            when(tagQueryService.getTagByIds(request.getTagIds())).thenReturn(List.of(tag));
            when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
                Post p = inv.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            Post result = postCommandService.createPost(user, request);

            assertThat(result.getTitle()).isEqualTo("My Title");
            assertThat(result.getAuthor()).isEqualTo(user);
            assertThat(result.getCategory()).isEqualTo(category);
            assertThat(result.getTags()).containsExactly(tag);
            assertThat(result.getStatus()).isEqualTo(PostStatusEnum.PUBLISHED);
            assertThat(result.getId()).isNotNull();
        }

        @Test
        @DisplayName("should calculate reading time based on word count")
        void shouldCalculateReadingTime() {
            // 400 words / 200 wpm = 2 minutes
            String content = "word ".repeat(400).trim();
            User user = buildUser();
            Category category = buildCategory();
            CreatePostRequest request = buildCreatePostRequest("Title", content, PostStatusEnum.DRAFT, category.getId(), Set.of());

            when(categoryQueryService.getCategoryById(any())).thenReturn(category);
            when(tagQueryService.getTagByIds(any())).thenReturn(List.of());
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.createPost(user, request);

            assertThat(result.getReadingTime()).isEqualTo(2);
        }

        @Test
        @DisplayName("should round reading time up for partial minutes")
        void shouldRoundReadingTimeUp() {
            // 201 words / 200 wpm = 1.005 → ceil = 2
            String content = "word ".repeat(201).trim();
            User user = buildUser();
            Category category = buildCategory();
            CreatePostRequest request = buildCreatePostRequest("Title", content, PostStatusEnum.DRAFT, category.getId(), Set.of());

            when(categoryQueryService.getCategoryById(any())).thenReturn(category);
            when(tagQueryService.getTagByIds(any())).thenReturn(List.of());
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.createPost(user, request);

            assertThat(result.getReadingTime()).isEqualTo(2);
        }

        @Test
        @DisplayName("should set reading time to 0 for null content")
        void shouldSetReadingTimeToZeroForNullContent() {
            User user = buildUser();
            Category category = buildCategory();
            CreatePostRequest request = buildCreatePostRequest("Title", null, PostStatusEnum.DRAFT, category.getId(), Set.of());

            when(categoryQueryService.getCategoryById(any())).thenReturn(category);
            when(tagQueryService.getTagByIds(any())).thenReturn(List.of());
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.createPost(user, request);

            assertThat(result.getReadingTime()).isEqualTo(0);
        }

        @Test
        @DisplayName("should set reading time to 0 for empty content")
        void shouldSetReadingTimeToZeroForEmptyContent() {
            User user = buildUser();
            Category category = buildCategory();
            CreatePostRequest request = buildCreatePostRequest("Title", "", PostStatusEnum.DRAFT, category.getId(), Set.of());

            when(categoryQueryService.getCategoryById(any())).thenReturn(category);
            when(tagQueryService.getTagByIds(any())).thenReturn(List.of());
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.createPost(user, request);

            assertThat(result.getReadingTime()).isEqualTo(0);
        }

        @Test
        @DisplayName("should persist post via repository")
        void shouldSavePostViaRepository() {
            User user = buildUser();
            Category category = buildCategory();
            CreatePostRequest request = buildCreatePostRequest("Title", "content", PostStatusEnum.PUBLISHED, category.getId(), Set.of());

            when(categoryQueryService.getCategoryById(any())).thenReturn(category);
            when(tagQueryService.getTagByIds(any())).thenReturn(List.of());
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postCommandService.createPost(user, request);

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository, times(1)).save(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo("Title");
        }
    }

    @Nested
    @DisplayName("updatePost")
    class UpdatePost {

        @Test
        @DisplayName("should update and return post when found")
        void shouldUpdatePostWhenFound() {
            UUID id = UUID.randomUUID();
            Category category = buildCategory();
            Tag tag = buildTag();

            Post existing = buildPost(id, "Old Title", "old content", category, Set.of(tag));
            UpdatePostRequest request = buildUpdatePostRequest("New Title", "new content", PostStatusEnum.PUBLISHED, category.getId(), Set.of(tag.getId()));

            when(postRepository.findById(id)).thenReturn(Optional.of(existing));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.updatePost(id, request);

            assertThat(result.getTitle()).isEqualTo("New Title");
            assertThat(result.getContent()).isEqualTo("new content");
            assertThat(result.getStatus()).isEqualTo(PostStatusEnum.PUBLISHED);
        }

        @Test
        @DisplayName("should recalculate reading time when content changes")
        void shouldRecalculateReadingTimeOnUpdate() {
            UUID id = UUID.randomUUID();
            Category category = buildCategory();
            // 400 words / 200 wpm = 2 minutes
            String newContent = "word ".repeat(400).trim();

            Post existing = buildPost(id, "Title", "old content", category, Set.of());
            UpdatePostRequest request = buildUpdatePostRequest("Title", newContent, PostStatusEnum.PUBLISHED, category.getId(), Set.of());

            when(postRepository.findById(id)).thenReturn(Optional.of(existing));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.updatePost(id, request);

            assertThat(result.getReadingTime()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw PostNotFoundException when post does not exist")
        void shouldThrowWhenPostNotFound() {
            UUID id = UUID.randomUUID();
            when(postRepository.findById(id)).thenReturn(Optional.empty());

            UpdatePostRequest request = buildUpdatePostRequest("Title", "content", PostStatusEnum.PUBLISHED, UUID.randomUUID(), Set.of());

            assertThatThrownBy(() -> postCommandService.updatePost(id, request)).isInstanceOf(PostNotFoundException.class).hasMessageContaining(id.toString());
        }

        @Test
        @DisplayName("should update category when category changes")
        void shouldUpdateCategoryWhenChanged() {
            UUID id = UUID.randomUUID();
            Category oldCategory = buildCategory();
            Category newCategory = buildCategory();

            Post existing = buildPost(id, "Title", "content", oldCategory, Set.of());
            UpdatePostRequest request = buildUpdatePostRequest("Title", "content", PostStatusEnum.PUBLISHED, newCategory.getId(), Set.of());

            when(postRepository.findById(id)).thenReturn(Optional.of(existing));
            when(categoryQueryService.getCategoryById(newCategory.getId())).thenReturn(newCategory);
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.updatePost(id, request);

            assertThat(result.getCategory()).isEqualTo(newCategory);
            verify(categoryQueryService, times(1)).getCategoryById(newCategory.getId());
        }

        @Test
        @DisplayName("should not fetch category when category is unchanged")
        void shouldNotFetchCategoryWhenUnchanged() {
            UUID id = UUID.randomUUID();
            Category category = buildCategory();
            Tag tag = buildTag();

            Post existing = buildPost(id, "Title", "content", category, Set.of(tag));
            UpdatePostRequest request = buildUpdatePostRequest("Title", "content", PostStatusEnum.PUBLISHED, category.getId(), Set.of(tag.getId()));

            when(postRepository.findById(id)).thenReturn(Optional.of(existing));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postCommandService.updatePost(id, request);

            verify(categoryQueryService, never()).getCategoryById(any());
        }

        @Test
        @DisplayName("should update tags when tag set changes")
        void shouldUpdateTagsWhenChanged() {
            UUID id = UUID.randomUUID();
            Category category = buildCategory();
            Tag oldTag = buildTag();
            Tag newTag = buildTag();

            Post existing = buildPost(id, "Title", "content", category, Set.of(oldTag));
            UpdatePostRequest request = buildUpdatePostRequest("Title", "content", PostStatusEnum.PUBLISHED, category.getId(), Set.of(newTag.getId()));

            when(postRepository.findById(id)).thenReturn(Optional.of(existing));
            when(tagQueryService.getTagByIds(Set.of(newTag.getId()))).thenReturn(List.of(newTag));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Post result = postCommandService.updatePost(id, request);

            assertThat(result.getTags()).containsExactly(newTag);
            verify(tagQueryService, times(1)).getTagByIds(Set.of(newTag.getId()));
        }

        @Test
        @DisplayName("should not fetch tags when tag set is unchanged")
        void shouldNotFetchTagsWhenUnchanged() {
            UUID id = UUID.randomUUID();
            Category category = buildCategory();
            Tag tag = buildTag();

            Post existing = buildPost(id, "Title", "content", category, Set.of(tag));
            UpdatePostRequest request = buildUpdatePostRequest("Title", "content", PostStatusEnum.PUBLISHED, category.getId(), Set.of(tag.getId()));

            when(postRepository.findById(id)).thenReturn(Optional.of(existing));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postCommandService.updatePost(id, request);

            verify(tagQueryService, never()).getTagByIds(any());
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("should delete post when found")
        void shouldDeletePostWhenFound() {
            UUID id = UUID.randomUUID();
            Category category = buildCategory();
            Post post = buildPost(id, "Title", "content", category, Set.of());

            when(postRepository.findById(id)).thenReturn(Optional.of(post));

            postCommandService.deletePost(id);

            verify(postRepository, times(1)).delete(post);
        }

        @Test
        @DisplayName("should throw PostNotFoundException when post does not exist")
        void shouldThrowWhenPostNotFound() {
            UUID id = UUID.randomUUID();
            when(postRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postCommandService.deletePost(id)).isInstanceOf(PostNotFoundException.class).hasMessageContaining(id.toString());

            verify(postRepository, never()).delete(any());
        }
    }

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    private Category buildCategory() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        return category;
    }

    private Tag buildTag() {
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        return tag;
    }

    private Post buildPost(UUID id, String title, String content, Category category, Set<Tag> tags) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setTags(tags);
        return post;
    }

    private CreatePostRequest buildCreatePostRequest(String title, String content, PostStatusEnum status, UUID categoryId, Set<UUID> tagIds) {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setStatus(status);
        request.setCategoryId(categoryId);
        request.setTagIds(tagIds);
        return request;
    }

    private UpdatePostRequest buildUpdatePostRequest(String title, String content, PostStatusEnum status, UUID categoryId, Set<UUID> tagIds) {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setStatus(status);
        request.setCategoryId(categoryId);
        request.setTagIds(tagIds);
        return request;
    }
}
