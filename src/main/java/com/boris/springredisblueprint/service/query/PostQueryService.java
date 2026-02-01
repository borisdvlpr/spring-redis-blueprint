package com.boris.springredisblueprint.service.query;

import com.boris.springredisblueprint.model.dto.PostDto;
import com.boris.springredisblueprint.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostQueryService {
    Page<PostDto> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable);

    PostDto getPost(UUID id);

    Page<PostDto> getDraftPosts(User user, Pageable pageable);
}
