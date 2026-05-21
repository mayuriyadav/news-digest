package com.news.digest.app.notification;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePreferenceRequest {

    @NotNull
    private NotificationType notificationType;

    @NotNull
    private NotificationChannel channel;

    @NotNull
    private Boolean isEnabled;
}
