package com.boris.springredisblueprint.service.impl;

import com.boris.springredisblueprint.exception.TagNotFoundException;
import com.boris.springredisblueprint.model.entities.Tag;
import com.boris.springredisblueprint.repository.TagRepository;
import com.boris.springredisblueprint.service.TagService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public List<Tag> createTags(Set<String> tagNames) {
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());

        List<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> Tag.builder()
                        .name(name)
                        .posts(new HashSet<>())
                        .build())
                .toList();

        List<Tag> savedTags = new ArrayList<>();
        if (!newTags.isEmpty()) {
            savedTags = tagRepository.saveAll(newTags);
        }

        savedTags.addAll(existingTags);

        return savedTags;
    }

    @Override
    public List<Tag> getTags() {
        return tagRepository.findAllWithPostCount();
    }

    @Override
    public Tag getTagById(UUID id) {
        return tagRepository.findById(id).orElseThrow(() ->
                new TagNotFoundException(String.format("Tag with ID '%s' not found.", id)));
    }

    @Override
    public List<Tag> getTagByIds(Set<UUID> ids) {
        List<Tag> foundTags = tagRepository.findAllById(ids);
        if (foundTags.size() != ids.size()) {
            throw new TagNotFoundException("Not all specified tag IDs exist.");
        }

        return foundTags;
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        tagRepository.findById(id).ifPresent(tag -> {
            if (!tag.getPosts().isEmpty()) {
                throw new IllegalStateException("Cannot delete tag with posts.");
            }

            tagRepository.deleteById(id);
        });
    }
}
