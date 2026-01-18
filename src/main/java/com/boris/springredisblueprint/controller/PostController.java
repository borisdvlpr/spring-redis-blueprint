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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostMapper postMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID tagId
    ) {
        List<Post> posts = postService.getAllPosts(categoryId, tagId);
        List<PostDTO> postDtos = posts.stream().map(postMapper::toDto).toList();

        return ResponseEntity.ok(postDtos);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDTO> getPost(
            @PathVariable UUID id
    ) {
        PostDTO postDto = postService.getPost(id);

        return ResponseEntity.ok(postDto);
    }

    @GetMapping(path = "/drafts")
    public ResponseEntity<List<PostDTO>> getDrafts(@RequestAttribute UUID userId) {
        User loggedInUser = userService.getUserById(userId);
        List<Post> draftPosts = postService.getDraftPosts(loggedInUser);
        List<PostDTO> postDtos = draftPosts.stream().map(postMapper::toDto).toList();

        return ResponseEntity.ok(postDtos);
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
