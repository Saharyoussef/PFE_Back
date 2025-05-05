package com.sahar.dashboardservice.service;

import com.sahar.dashboardservice.model.User;


public interface UserService {
    User getUserByUuid(String userUuid);
}