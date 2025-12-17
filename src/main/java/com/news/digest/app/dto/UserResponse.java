package com.news.digest.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long userId;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
