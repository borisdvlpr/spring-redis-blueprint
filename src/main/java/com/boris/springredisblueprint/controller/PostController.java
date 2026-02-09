package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.mapper.PostMapper;
import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
import com.boris.springredisblueprint.model.dto.CreatePostRequestDto;
import com.boris.springredisblueprint.model.dto.PostDto;
import com.boris.springredisblueprint.model.dto.UpdatePostRequestDto;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.User;
import com.boris.springredisblueprint.service.UserService;
import com.boris.springredisblueprint.service.command.PostCommandService;
import com.boris.springredisblueprint.service.query.PostQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final PostMapper postMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId,
            Pageable pageable
    ) {
        log.info("GET /api/v1/posts - categoryId: {}, tagId: {}, page: {}",
                categoryId, tagId, pageable.getPageNumber());

        Page<PostDto> posts = postQueryService.getAllPosts(categoryId, tagId, pageable);

        log.debug("Returning {} posts", posts.getNumberOfElements());
        return ResponseEntity.ok(posts);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable UUID id) {
        log.info("GET /api/v1/posts/{}", id);
        PostDto postDto = postQueryService.getPost(id);

        log.debug("Returning post - title: '{}', id: '{}'",
                postDto.getTitle(), postDto.getId());

        return ResponseEntity.ok(postDto);
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<Page<PostDto>> getDrafts(
            @RequestAttribute UUID userId,
            Pageable pageable
    ) {
        log.info("GET /api/v1/posts/drafts - userId: {}, page: {}",
                userId, pageable.getPageNumber());

        User loggedInUser = userService.getUserById(userId);
        Page<PostDto> draftPosts = postQueryService.getDraftPosts(loggedInUser, pageable);

        log.debug("Returning {} draft posts", draftPosts.getNumberOfElements());
        return ResponseEntity.ok(draftPosts);
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequestDto createPostRequestDTO,
            @RequestAttribute UUID userId
    ) {
        log.info("POST /api/v1/posts - title: '{}', userId: {}",
                createPostRequestDTO.getTitle(), userId);

        User loggedInUser = userService.getUserById(userId);
        CreatePostRequest createPostRequest = postMapper.toCreatePostRequest(createPostRequestDTO);

        Post createdPost = postCommandService.createPost(loggedInUser, createPostRequest);
        PostDto createdPostDto = postMapper.toDto(createdPost);

        log.debug("Returning saved post - title: '{}', id: '{}'",
                createdPostDto.getTitle(), createdPostDto.getId());

        return new ResponseEntity<>(createdPostDto, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequestDto updatePostRequestDTO
    ) {
        log.info("PUT /api/v1/posts/{} - title: '{}'",
                id, updatePostRequestDTO.getTitle());

        UpdatePostRequest updatePostRequest = postMapper.toUpdatePostRequest(updatePostRequestDTO);

        Post updatedPost = postCommandService.updatePost(id, updatePostRequest);
        PostDto updatedPostDto = postMapper.toDto(updatedPost);

        log.debug("Returning updated post - title: '{}', id: '{}'",
                updatedPostDto.getTitle(), updatedPostDto.getId());

        return ResponseEntity.ok(updatedPostDto);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        log.info("DELETE /api/v1/posts/{}", id);
        postCommandService.deletePost(id);

        log.debug("Deleted post - id: '{}'", id);
        return ResponseEntity.noContent().build();
    }
}
