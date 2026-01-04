package com.boris.springredisblueprint.service;

import com.boris.springredisblueprint.domain.entities.User;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
}
