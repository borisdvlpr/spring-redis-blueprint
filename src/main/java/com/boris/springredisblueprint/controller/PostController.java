package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.domain.CreatePostRequest;
import com.boris.springredisblueprint.domain.UpdatePostRequest;
import com.boris.springredisblueprint.domain.dto.CreatePostRequestDTO;
import com.boris.springredisblueprint.domain.dto.PostDTO;
import com.boris.springredisblueprint.domain.dto.UpdatePostRequestDTO;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.User;
import com.boris.springredisblueprint.mapper.PostMapper;
import com.boris.springredisblueprint.service.PostService;
import com.boris.springredisblueprint.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostMapper postMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId,
            Pageable pageable
    ) {
        Page<Post> posts = postService.getAllPosts(categoryId, tagId, pageable);

        return ResponseEntity.ok(posts.map(postMapper::toDto));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDTO> getPost(
            @PathVariable UUID id
    ) {
        PostDTO postDto = postService.getPost(id);

        return ResponseEntity.ok(postDto);
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<Page<PostDTO>> getDrafts(@RequestAttribute UUID userId, Pageable pageable) {
        User loggedInUser = userService.getUserById(userId);
        Page<Post> draftPosts = postService.getDraftPosts(loggedInUser, pageable);

        return ResponseEntity.ok(draftPosts.map(postMapper::toDto));
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @Valid @RequestBody CreatePostRequestDTO createPostRequestDTO,
            @RequestAttribute UUID userId
    ) {
        User loggedInUser = userService.getUserById(userId);
        CreatePostRequest createPostRequest = postMapper.toCreatePostRequest(createPostRequestDTO);
        Post createdPost = postService.createPost(loggedInUser, createPostRequest);
        PostDTO createdPostDTO = postMapper.toDto(createdPost);

        return new ResponseEntity<>(createdPostDTO, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequestDTO updatePostRequestDTO
    ) {
        UpdatePostRequest updatePostRequest = postMapper.toUpdatePostRequest(updatePostRequestDTO);
        Post updatedPost = postService.updatePost(id, updatePostRequest);
        PostDTO updatedPostDto = postMapper.toDto(updatedPost);

        return ResponseEntity.ok(updatedPostDto);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID id
    ) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
