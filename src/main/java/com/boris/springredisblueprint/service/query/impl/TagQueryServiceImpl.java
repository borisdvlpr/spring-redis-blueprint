package com.boris.springredisblueprint.service.query.impl;

import com.boris.springredisblueprint.exception.TagNotFoundException;
import com.boris.springredisblueprint.mapper.TagMapper;
import com.boris.springredisblueprint.model.dto.TagDto;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.repository.TagRepository;
import com.boris.springredisblueprint.service.query.TagQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TagQueryServiceImpl implements TagQueryService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public List<TagDto> getTags() {
        List<TagDto> tags = tagRepository.findAllWithPostCount().stream()
                .map(tagMapper::toDto)
                .toList();

        logListSize(tags);

        return tags;
    }

    @Override
    public Tag getTagById(UUID id) {
        log.info("Fetching tag with id: {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tag not found with id: {}", id);
                    return new TagNotFoundException(
                            String.format("Tag with ID '%s' not found.", id));
                });

        log.info("Successfully fetched tag: {}", tag.getName());

        return tag;
    }

    @Override
    public List<Tag> getTagByIds(Set<UUID> ids) {
        log.info("Fetching tag with id: {}", ids.toArray());

        List<Tag> foundTags = tagRepository.findAllById(ids);
        if (foundTags.size() != ids.size()) {
            throw new TagNotFoundException("Not all specified tag IDs exist.");
        }

        logListSize(foundTags);

        return foundTags;
    }

    private void logListSize(List<?> list) {
        log.info("Found {} tags", list.size());
    }
}
