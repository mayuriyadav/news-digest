package com.news.digest.app.dto;

import com.news.digest.app.notification.NotificationChannel;
import com.news.digest.app.notification.NotificationType;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPreferenceDTO {

    private NotificationType notificationType;
    private NotificationChannel channel;
    private Boolean isEnabled;
}
