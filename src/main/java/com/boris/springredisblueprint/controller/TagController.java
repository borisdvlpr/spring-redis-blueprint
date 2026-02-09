package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.mapper.TagMapper;
import com.boris.springredisblueprint.model.dto.CreateTagsRequestDto;
import com.boris.springredisblueprint.model.dto.TagDto;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.service.command.TagCommandService;
import com.boris.springredisblueprint.service.query.TagQueryService;
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
public class TagController {
    private final TagCommandService tagCommandService;
    private final TagQueryService tagQueryService;
    private final TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        log.info("GET /api/v1/tags");
        List<Tag> tags = tagQueryService.getTags();
        List<TagDto> tagRespons = tags.stream().map(tagMapper::toTagResponse).toList();

        log.debug("Returning {} tags", tags.size());
        return ResponseEntity.ok(tagRespons);
    }

    @PostMapping
    public ResponseEntity<List<TagDto>> createTags(@RequestBody CreateTagsRequestDto createTagsRequestDto) {
        log.info("POST /api/v1/tags");
        List<Tag> savedTags = tagCommandService.createTags(createTagsRequestDto.getNames());
        List<TagDto> createdTagsResponse = savedTags.stream().map(tagMapper::toTagResponse).toList();

        log.debug("Created {} tags", createdTagsResponse.size());
        return new ResponseEntity<>(
                createdTagsResponse,
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        log.info("DELETE /api/v1/tags/{}", id);
        tagCommandService.deleteTag(id);

        log.debug("Deleted tag - id: '{}'", id);
        return ResponseEntity.noContent().build();
    }

}
