package com.news.digest.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {


    @NotBlank(message = "Content must not be blank")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;

    private Long parentId; // null for new comment, set for reply
}
