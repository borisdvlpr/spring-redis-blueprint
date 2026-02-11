package com.boris.springredisblueprint.service.command.impl;

import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.repository.TagRepository;
import com.boris.springredisblueprint.service.command.TagCommandService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TagCommandServiceImpl implements TagCommandService {
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public List<Tag> createTags(Set<String> tagNames) {
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());

        log.info("Found {} existing tags: {}", existingTags.size(), existingTagNames);

        List<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> Tag.builder()
                        .name(name)
                        .posts(new HashSet<>())
                        .build())
                .toList();

        List<Tag> savedTags = new ArrayList<>();
        if (!newTags.isEmpty()) {
            log.info("Saving {} new tags", newTags.size());
            savedTags = tagRepository.saveAll(newTags);
        }

        savedTags.addAll(existingTags);
        log.info("Returning {} total tags", savedTags.size());

        return savedTags;
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        log.info("Deleting tag with id: '{}'", id);

        tagRepository.findById(id).ifPresent(tag -> {
            if (!tag.getPosts().isEmpty()) {
                throw new IllegalStateException("Cannot delete tag with posts.");
            }

            tagRepository.deleteById(id);
            log.info("Successfully deleted tag: '{}'", id);
        });
    }
}
