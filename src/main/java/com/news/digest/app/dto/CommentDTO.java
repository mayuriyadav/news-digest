package com.news.digest.app.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Long articleId;
    private Long userId;
    private String userName;
    private Long parentId;          // null = top-level comment, non-null = reply
    private String content;
    private Integer likeCount;
    private Boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDTO> replies; // nested replies (top-level only)
}
