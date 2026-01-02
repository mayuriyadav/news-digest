package com.news.digest.app.service;

import com.news.digest.app.dto.RegisterRequest;
import com.news.digest.app.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest registerRequest);
}
