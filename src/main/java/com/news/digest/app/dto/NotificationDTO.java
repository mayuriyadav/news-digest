package com.news.digest.app.dto;
import com.news.digest.app.notification.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long articleId;
    private String articleTitle;
    private String articleUrl;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
