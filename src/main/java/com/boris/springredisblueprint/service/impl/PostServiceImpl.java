package com.boris.springredisblueprint.service.impl;

import com.boris.springredisblueprint.domain.CreatePostRequest;
import com.boris.springredisblueprint.domain.PostStatus;
import com.boris.springredisblueprint.domain.UpdatePostRequest;
import com.boris.springredisblueprint.domain.dto.PostDTO;
import com.boris.springredisblueprint.domain.entities.Category;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.Tag;
import com.boris.springredisblueprint.domain.entities.User;
import com.boris.springredisblueprint.mapper.PostMapper;
import com.boris.springredisblueprint.repository.PostRepository;
import com.boris.springredisblueprint.service.CategoryService;
import com.boris.springredisblueprint.service.PostService;
import com.boris.springredisblueprint.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PostRepository postRepository;

    private static final int WORDS_PER_MINUTE = 200;
    private final PostMapper postMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable) {
        if (categoryId != null && tagId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            Tag tag = tagService.getTagById(tagId);

            return postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category, tag, pageable);
        }

        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            return postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category, pageable);
        }

        if (tagId != null) {
            Tag tag = tagService.getTagById(tagId);
            return postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag, pageable);
        }

        return postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "POST_CACHE", key = "#id")
    public PostDTO getPost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found."));

        return postMapper.toDto(post);
    }

    @Override
    public Page<Post> getDraftPosts(User user, Pageable pageable) {
        return postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT, pageable);
    }

    @Override
    @Transactional
    @CachePut(value = "POST_CACHE", key = "#result.id()")
    public Post createPost(User user, CreatePostRequest createPostRequest) {
        Post newPost = new Post();
        newPost.setTitle(createPostRequest.getTitle());
        newPost.setContent(createPostRequest.getContent());
        newPost.setStatus(createPostRequest.getStatus());
        newPost.setAuthor(user);
        newPost.setReadingTime(calculateReadingTime(createPostRequest.getContent()));

        Category category = categoryService.getCategoryById(createPostRequest.getCategoryId());
        newPost.setCategory(category);

        Set<UUID> tagIds = createPostRequest.getTagIds();
        List<Tag> tags = tagService.getTagByIds(tagIds);
        newPost.setTags(new HashSet<>(tags));

        return postRepository.save(newPost);
    }

    @Override
    @Transactional
    @CachePut(value = "POST_CACHE", key = "#result.id()")
    public Post updatePost(UUID id, UpdatePostRequest updatePostRequest) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post does not exist with id: " + id));

        existingPost.setTitle(updatePostRequest.getTitle());
        String postContent = updatePostRequest.getContent();
        existingPost.setContent(postContent);
        existingPost.setStatus(updatePostRequest.getStatus());
        existingPost.setReadingTime(calculateReadingTime(postContent));

        UUID updatePostRequestCategoryId = updatePostRequest.getCategoryId();
        if (!existingPost.getCategory().getId().equals(updatePostRequestCategoryId)) {
            Category newCategory = categoryService.getCategoryById(updatePostRequestCategoryId);
            existingPost.setCategory(newCategory);
        }

        Set<UUID> existingTagIds = existingPost.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        Set<UUID> updatePostRequestTagsIds = updatePostRequest.getTagIds();
        if (!existingTagIds.equals(updatePostRequestTagsIds)) {
            List<Tag> newTags = tagService.getTagByIds(updatePostRequestTagsIds);
            existingPost.setTags(new HashSet<>(newTags));
        }

        return postRepository.save(existingPost);
    }

    @Override
    @CacheEvict(value = "POST_CACHE", key = "#id")
    public void deletePost(UUID id) {
        PostDTO deletePostDto = getPost(id);
        Post deletePost = postMapper.fromDto(deletePostDto);
        postRepository.delete(deletePost);
    }

    private Integer calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int wordCount = content.trim().split("\\s+").length;
        return (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);
    }
}
