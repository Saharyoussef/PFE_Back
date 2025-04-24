package com.sahar.discoveryservice.repository;

import com.sahar.discoveryservice.model.User;

public interface UserRepository {
    User getUserByUsername(String username);
}
