package com.news.digest.app.serviceimpl;

import com.news.digest.app.dto.AuthResponse;
import com.news.digest.app.dto.LoginRequest;
import com.news.digest.app.dto.RegisterRequest;
import com.news.digest.app.dto.UserResponse;
import com.news.digest.app.exception.ResourceNotFoundException;
import com.news.digest.app.model.User;
import com.news.digest.app.repository.UserRepository;
import com.news.digest.app.security.JwtUtil;
import com.news.digest.app.service.NotificationService;
import com.news.digest.app.service.UserService;
import com.news.digest.app.exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    @Lazy
    private final NotificationService notificationService ;
    @Override
    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByUserName(registerRequest.getUserName())) {
            throw new ResourceAlreadyExistsException("Username already taken");
        }


        User user = new User();
        user.setUserName(registerRequest.getUserName());
        user.setEmail(registerRequest.getEmail());

        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = userRepository.save(user);

        notificationService.initDefaultPreferences(savedUser.getId());
        // Convert to response
        return convertToResponse(savedUser);
    }
    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new ResourceNotFoundException("User", "email", request.getEmail()));
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token,"Bearer", convertToResponse(user));
    }
    @Override
    public UserResponse getCurrentUser() {
        //get email from securityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new  ResourceNotFoundException("User", "email", email));
        return convertToResponse(user);
    }



    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId());
        response.setUserName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());
        return response;

    }
}