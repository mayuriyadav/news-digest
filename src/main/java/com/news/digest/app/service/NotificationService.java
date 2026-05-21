package com.news.digest.app.service;

import com.news.digest.app.dto.NotificationDTO;
import com.news.digest.app.dto.NotificationPreferenceDTO;
import com.news.digest.app.dto.NotificationSummaryDTO;
import com.news.digest.app.notification.UpdatePreferenceRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    // ---- Delivery ----
    void sendBreakingNewsAlert(Long articleId, String category);
    void sendTrendingAlert(Long articleId);
    void sendCommentReplyAlert(Long commentId, Long parentCommentUserId);
    void sendWeeklyDigest();

    // ---- In-app inbox ----
    Page<NotificationDTO> getNotifications(Long userId, int page, int size);
    Page<NotificationDTO> getUnreadNotifications(Long userId, int page, int size);
    NotificationSummaryDTO getSummary(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    void deleteOldNotifications(Long userId);

    // ---- Preferences ----
    List<NotificationPreferenceDTO> getPreferences(Long userId);
    NotificationPreferenceDTO updatePreference(Long userId, UpdatePreferenceRequest request);
    void initDefaultPreferences(Long userId);
}
