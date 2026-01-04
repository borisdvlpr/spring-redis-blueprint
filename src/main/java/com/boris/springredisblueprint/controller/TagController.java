package com.boris.springredisblueprint.controller;

import com.boris.springredisblueprint.domain.dto.CreateTagsRequest;
import com.boris.springredisblueprint.domain.dto.TagDTO;
import com.boris.springredisblueprint.domain.entities.Tag;
import com.boris.springredisblueprint.mapper.TagMapper;
import com.boris.springredisblueprint.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;
    private final TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<Tag> tags = tagService.getTags();
        List<TagDTO> tagRespons = tags.stream().map(tagMapper::toTagResponse).toList();

        return ResponseEntity.ok(tagRespons);
    }

    @PostMapping
    public ResponseEntity<List<TagDTO>> createTags(@RequestBody CreateTagsRequest createTagsRequest) {
        List<Tag> savedTags = tagService.createTags(createTagsRequest.getNames());
        List<TagDTO> createdTagRespons = savedTags.stream().map(tagMapper::toTagResponse).toList();

        return new ResponseEntity<>(
                createdTagRespons,
                HttpStatus.CREATED
        );
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

}
