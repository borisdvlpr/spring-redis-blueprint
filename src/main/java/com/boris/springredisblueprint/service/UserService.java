package com.boris.springredisblueprint.service;

import com.boris.springredisblueprint.model.entity.User;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
}
