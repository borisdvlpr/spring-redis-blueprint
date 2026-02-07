package com.boris.springredisblueprint.service.command;

import com.boris.springredisblueprint.model.entity.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TagCommandService {
    List<Tag> createTags(Set<String> tagNames);

    void deleteTag(UUID id);
}
