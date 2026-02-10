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
import com.boris.springredisblueprint.service.query.PostQueryService;
import com.boris.springredisblueprint.service.query.TagQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CategoryQueryService categoryQueryService;
    private final TagQueryService tagQueryService;

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable) {
        log.info("Querying posts with categoryId: {}, tagId: {}, page: {}",
                categoryId, tagId, pageable.getPageNumber());

        Page<Post> posts;

        if (categoryId != null && tagId != null) {
            Category category = categoryQueryService.getCategoryById(categoryId);
            Tag tag = tagQueryService.getTagById(tagId);
            posts = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatusEnum.PUBLISHED, category, tag, pageable);
        } else if (categoryId != null) {
            Category category = categoryQueryService.getCategoryById(categoryId);
            posts = postRepository.findAllByStatusAndCategory(
                    PostStatusEnum.PUBLISHED, category, pageable);
        } else if (tagId != null) {
            Tag tag = tagQueryService.getTagById(tagId);
            posts = postRepository.findAllByStatusAndTagsContaining(
                    PostStatusEnum.PUBLISHED, tag, pageable);
        } else {
            posts = postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, pageable);
        }

        log.info("Found {} posts", posts.getTotalElements());
        return posts.map(postMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "POST_CACHE", key = "#id")
    public PostDto getPost(UUID id) {
        log.info("Fetching post with id: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Post not found with id: {}", id);
                    return new PostNotFoundException(
                            String.format("Post with ID '%s' not found.", id));
                });

        log.info("Successfully fetched post: {}", post.getTitle());
        return postMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getDraftPosts(User user, Pageable pageable) {
        log.info("Fetching draft posts for user: {}", user.getId());

        Page<Post> draftPosts = postRepository.findAllByAuthorAndStatus(
                user, PostStatusEnum.DRAFT, pageable);

        log.info("Found {} draft posts for user {}",
                draftPosts.getTotalElements(), user.getId());
        return draftPosts.map(postMapper::toDto);
    }
}
