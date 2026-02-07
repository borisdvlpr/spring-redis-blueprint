package com.boris.springredisblueprint.service.query;

import com.boris.springredisblueprint.model.entity.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TagQueryService {
    List<Tag> getTags();

    Tag getTagById(UUID id);

    List<Tag> getTagByIds(Set<UUID> ids);
}
