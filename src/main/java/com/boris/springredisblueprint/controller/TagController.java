package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.mapper.TagMapper;
import com.boris.springredisblueprint.model.dto.ApiErrorResponse;
import com.boris.springredisblueprint.model.dto.CreateTagsRequestDto;
import com.boris.springredisblueprint.model.dto.TagDto;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.service.command.TagCommandService;
import com.boris.springredisblueprint.service.query.TagQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping(path = "/api/v1/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Manage post tags")
public class TagController {
    private final TagCommandService tagCommandService;
    private final TagQueryService tagQueryService;
    private final TagMapper tagMapper;

    @Operation(summary = "Get all tags", description = "Returns a list of all available tags")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        log.info("GET /api/v1/tags");
        List<TagDto> tags = tagQueryService.getTags();

        log.debug("Returning {} tags", tags.size());
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Create tags", description = "Creates one or more new tags from a list of names")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tags created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "One or more tags already exist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(@RequestBody CreateTagsRequestDto createTagsRequestDto) {
        log.info("POST /api/v1/tags");
        List<Tag> savedTags = tagCommandService.createTags(createTagsRequestDto.getNames());
        List<TagDto> createdTagsResponse = savedTags.stream().map(tagMapper::toDto).toList();

        log.debug("Created {} tags", createdTagsResponse.size());
        return new ResponseEntity<>(
                createdTagsResponse,
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Delete a tag", description = "Deletes a tag by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "UUID of the tag to delete") @PathVariable UUID id
    ) {
        log.info("DELETE /api/v1/tags/{}", id);
        tagCommandService.deleteTag(id);

        log.debug("Deleted tag - id: '{}'", id);
        return ResponseEntity.noContent().build();
    }
}
