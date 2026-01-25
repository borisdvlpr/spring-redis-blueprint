package com.boris.springredisblueprint.service;

import com.boris.springredisblueprint.model.entities.User;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
}
