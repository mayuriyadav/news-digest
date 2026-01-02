package com.news.digest.app.service.impl;

import com.news.digest.app.dto.RegisterRequest;
import com.news.digest.app.dto.UserResponse;
import com.news.digest.app.repository.UserRepository;
import com.news.digest.app.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserResponse register(RegisterRequest registerRequest) {
        if (UserRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        if (UserRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already taken");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save to database
        User savedUser = userRepository.save(user);

        // Convert to response
        return convertToResponse(savedUser);
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        return response;

    }
}
