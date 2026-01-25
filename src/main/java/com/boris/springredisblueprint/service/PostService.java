package com.boris.springredisblueprint.service;


import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
import com.boris.springredisblueprint.model.dto.PostDto;
import com.boris.springredisblueprint.model.entities.Post;
import com.boris.springredisblueprint.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostService {
    Page<Post> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable);

    PostDto getPost(UUID id);

    Page<Post> getDraftPosts(User user, Pageable pageable);

    Post createPost(User user, CreatePostRequest createPostRequest);

    Post updatePost(UUID id, UpdatePostRequest updatePostRequest);

    void deletePost(UUID id);
}
