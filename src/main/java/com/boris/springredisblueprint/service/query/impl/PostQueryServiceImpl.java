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
import com.boris.springredisblueprint.service.CategoryService;
import com.boris.springredisblueprint.service.TagService;
import com.boris.springredisblueprint.service.query.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CategoryService categoryService;
    private final TagService tagService;

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable) {
        Page<Post> posts;

        if (categoryId != null && tagId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            Tag tag = tagService.getTagById(tagId);
            posts = postRepository.findAllByStatusAndCategoryAndTagsContaining(
                    PostStatusEnum.PUBLISHED, category, tag, pageable);
        } else if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            posts = postRepository.findAllByStatusAndCategory(
                    PostStatusEnum.PUBLISHED, category, pageable);
        } else if (tagId != null) {
            Tag tag = tagService.getTagById(tagId);
            posts = postRepository.findAllByStatusAndTagsContaining(
                    PostStatusEnum.PUBLISHED, tag, pageable);
        } else {
            posts = postRepository.findAllByStatus(PostStatusEnum.PUBLISHED, pageable);
        }

        return posts.map(postMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "POST_CACHE", key = "#id")
    public PostDto getPost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(
                        String.format("Post with ID '%s' not found.", id)));

        return postMapper.toDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getDraftPosts(User user, Pageable pageable) {
        Page<Post> draftPosts = postRepository.findAllByAuthorAndStatus(
                user, PostStatusEnum.DRAFT, pageable);

        return draftPosts.map(postMapper::toDto);
    }
}
