package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.mapper.PostMapper;
import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
import com.boris.springredisblueprint.model.dto.ApiErrorResponse;
import com.boris.springredisblueprint.model.dto.CreatePostRequestDto;
import com.boris.springredisblueprint.model.dto.PostDto;
import com.boris.springredisblueprint.model.dto.UpdatePostRequestDto;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.User;
import com.boris.springredisblueprint.service.UserService;
import com.boris.springredisblueprint.service.command.PostCommandService;
import com.boris.springredisblueprint.service.query.PostQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts", description = "Create, read, update and delete blog posts")
public class PostController {
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final PostMapper postMapper;
    private final UserService userService;

    @Operation(summary = "Get all posts", description = "Returns a paginated list of published posts, optionally filtered by category or tag")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by tag ID") @RequestParam(required = false) UUID tagId,
            Pageable pageable
    ) {
        log.info("GET /api/v1/posts - categoryId: {}, tagId: {}, page: {}",
                categoryId, tagId, pageable.getPageNumber());

        Page<PostDto> posts = postQueryService.getAllPosts(categoryId, tagId, pageable);

        logPageResult("posts", posts);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Get a post by ID", description = "Returns a single published post by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post found",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping(path = "/{id}")
    public ResponseEntity<PostDto> getPost(
            @Parameter(description = "UUID of the post to retrieve") @PathVariable UUID id
    ) {
        log.info("GET /api/v1/posts/{}", id);
        PostDto postDto = postQueryService.getPost(id);

        log.debug("Returning post - title: '{}', id: '{}'",
                postDto.getTitle(), postDto.getId());

        return ResponseEntity.ok(postDto);
    }

    @Operation(summary = "Get draft posts", description = "Returns a paginated list of draft posts belonging to the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Draft posts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/drafts")
    public ResponseEntity<Page<PostDto>> getDrafts(
            @Parameter(hidden = true) @RequestAttribute UUID userId,
            Pageable pageable
    ) {
        log.info("GET /api/v1/posts/drafts - userId: {}, page: {}",
                userId, pageable.getPageNumber());

        User loggedInUser = userService.getUserById(userId);
        Page<PostDto> draftPosts = postQueryService.getDraftPosts(loggedInUser, pageable);

        logPageResult("draft posts", draftPosts);
        return ResponseEntity.ok(draftPosts);
    }

    @Operation(summary = "Create a post", description = "Creates a new blog post attributed to the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created successfully",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody CreatePostRequestDto createPostRequestDTO,
            @Parameter(hidden = true) @RequestAttribute UUID userId
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

    @Operation(summary = "Update a post", description = "Updates an existing post by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated successfully",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/{id}")
    public ResponseEntity<PostDto> updatePost(
            @Parameter(description = "UUID of the post to update") @PathVariable UUID id,
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

    @Operation(summary = "Delete a post", description = "Deletes a post by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "UUID of the post to delete") @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/posts/{}", id);
        postCommandService.deletePost(id);

        log.debug("Deleted post - id: '{}'", id);
        return ResponseEntity.noContent().build();
    }

    private void logPageResult(String entityType, Page<?> page) {
        log.debug("Returning {} {} (page {}, total elements: {})",
                page.getNumberOfElements(),
                entityType,
                page.getNumber(),
                page.getTotalElements());
    }
}
