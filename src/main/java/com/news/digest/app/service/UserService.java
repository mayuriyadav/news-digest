package com.news.digest.app.service;

import com.news.digest.app.dto.AuthResponse;
import com.news.digest.app.dto.LoginRequest;
import com.news.digest.app.dto.RegisterRequest;
import com.news.digest.app.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest registerRequest);
    UserResponse getCurrentUser();
    AuthResponse login(LoginRequest request);
}
