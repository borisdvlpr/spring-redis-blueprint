package com.boris.springredisblueprint.service.command;

import com.boris.springredisblueprint.model.CreatePostRequest;
import com.boris.springredisblueprint.model.UpdatePostRequest;
import com.boris.springredisblueprint.model.entity.Post;
import com.boris.springredisblueprint.model.entity.User;

import java.util.UUID;

public interface PostCommandService {
    Post createPost(User user, CreatePostRequest createPostRequest);

    Post updatePost(UUID id, UpdatePostRequest updatePostRequest);

    void deletePost(UUID id);
}
