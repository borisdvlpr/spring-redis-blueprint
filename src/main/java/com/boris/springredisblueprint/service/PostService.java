package com.boris.springredisblueprint.service;


import com.boris.springredisblueprint.domain.CreatePostRequest;
import com.boris.springredisblueprint.domain.UpdatePostRequest;
import com.boris.springredisblueprint.domain.dto.PostDTO;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PostService {
    Page<Post> getAllPosts(UUID categoryId, UUID tagId, Pageable pageable);

    PostDTO getPost(UUID id);

    Page<Post> getDraftPosts(User user, Pageable pageable);

    Post createPost(User user, CreatePostRequest createPostRequest);

    Post updatePost(UUID id, UpdatePostRequest updatePostRequest);

    void deletePost(UUID id);
}
