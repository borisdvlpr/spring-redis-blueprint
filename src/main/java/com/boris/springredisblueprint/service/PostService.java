package com.boris.springredisblueprint.service;


import com.boris.springredisblueprint.domain.CreatePostRequest;
import com.boris.springredisblueprint.domain.UpdatePostRequest;
import com.boris.springredisblueprint.domain.dto.PostDTO;
import com.boris.springredisblueprint.domain.entities.Post;
import com.boris.springredisblueprint.domain.entities.User;

import java.util.List;
import java.util.UUID;

public interface PostService {
    List<Post> getAllPosts(UUID categoryId, UUID tagId);

    PostDTO getPost(UUID id);

    List<Post> getDraftPosts(User user);

    Post createPost(User user, CreatePostRequest createPostRequest);

    Post updatePost(UUID id, UpdatePostRequest updatePostRequest);

    void deletePost(UUID id);
}
