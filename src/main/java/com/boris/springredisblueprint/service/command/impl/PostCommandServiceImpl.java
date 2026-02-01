package com.boris.springredisblueprint.service.command.impl;

import com.boris.springredisblueprint.exception.PostNotFoundException;
import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
import com.boris.springredisblueprint.model.entity.Category;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.model.entity.User;
import com.boris.springredisblueprint.repository.PostRepository;
import com.boris.springredisblueprint.service.CategoryService;
import com.boris.springredisblueprint.service.TagService;
import com.boris.springredisblueprint.service.command.PostCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;
    private final CategoryService categoryService;
    private final TagService tagService;

    private static final int WORDS_PER_MINUTE = 200;

    @Override
    @Transactional
    public Post createPost(User user, CreatePostRequest createPostRequest) {
        log.info("Creating new post '{}' by user {}",
                createPostRequest.getTitle(), user.getId());

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

        Post savedPost = postRepository.save(newPost);
        log.info("Successfully created post with id: {}", savedPost.getId());

        return savedPost;
    }

    @Override
    @Transactional
    @CacheEvict(value = "POST_CACHE", key = "#id")
    public Post updatePost(UUID id, UpdatePostRequest updatePostRequest) {
        log.info("Updating post with id: {}", id);

        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Post not found for update: {}", id);
                    return new PostNotFoundException(
                            String.format("Post with ID '%s' not found.", id));
                });

        existingPost.setTitle(updatePostRequest.getTitle());
        String postContent = updatePostRequest.getContent();
        existingPost.setContent(postContent);
        existingPost.setStatus(updatePostRequest.getStatus());
        existingPost.setReadingTime(calculateReadingTime(postContent));

        UUID updatePostRequestCategoryId = updatePostRequest.getCategoryId();
        if (!existingPost.getCategory().getId().equals(updatePostRequestCategoryId)) {
            log.debug("Updating category for post {}", id);
            Category newCategory = categoryService.getCategoryById(updatePostRequestCategoryId);
            existingPost.setCategory(newCategory);
        }

        Set<UUID> existingTagIds = existingPost.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
        Set<UUID> updatePostRequestTagsIds = updatePostRequest.getTagIds();

        if (!existingTagIds.equals(updatePostRequestTagsIds)) {
            log.debug("Updating tags for post {}", id);
            List<Tag> newTags = tagService.getTagByIds(updatePostRequestTagsIds);
            existingPost.setTags(new HashSet<>(newTags));
        }

        Post updatedPost = postRepository.save(existingPost);
        log.info("Successfully updated post: {}", id);

        return updatedPost;
    }

    @Override
    @Transactional
    @CacheEvict(value = "POST_CACHE", key = "#id")
    public void deletePost(UUID id) {
        log.info("Deleting post with id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Post not found for deletion: {}", id);
                    return new PostNotFoundException(
                            String.format("Post with ID '%s' not found.", id));
                });

        postRepository.delete(post);
        log.info("Successfully deleted post: {}", id);
    }

    private Integer calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }

        int wordCount = content.trim().split("\\s+").length;
        return (int) Math.ceil((double) wordCount / WORDS_PER_MINUTE);
    }
}