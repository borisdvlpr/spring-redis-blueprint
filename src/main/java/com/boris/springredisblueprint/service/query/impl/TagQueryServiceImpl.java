package com.boris.springredisblueprint.service.query.impl;

import com.boris.springredisblueprint.exception.TagNotFoundException;
import com.boris.springredisblueprint.model.entity.Tag;
import com.boris.springredisblueprint.repository.TagRepository;
import com.boris.springredisblueprint.service.query.TagQueryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TagQueryServiceImpl implements TagQueryService {
    TagRepository tagRepository;

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
}
