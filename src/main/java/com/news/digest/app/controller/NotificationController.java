package com.news.digest.app.controller;


import com.news.digest.app.dto.ApiResponse;
import com.news.digest.app.dto.NotificationDTO;
import com.news.digest.app.dto.NotificationPreferenceDTO;
import com.news.digest.app.dto.NotificationSummaryDTO;
import com.news.digest.app.notification.UpdatePreferenceRequest;
import com.news.digest.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    private ResponseEntity<ApiResponse<Void>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
    }

    // ==================== INBOX ====================

    /**
     * GET /api/notifications
     * All notifications (paginated)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched",
                notificationService.getNotifications(userId, page, size)));
    }

    /**
     * GET /api/notifications/unread
     * Only unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUnread(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success("Unread notifications",
                notificationService.getUnreadNotifications(userId, page, size)));
    }

    /**
     * GET /api/notifications/summary
     * Unread count + 5 most recent — perfect for a bell icon dropdown
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<NotificationSummaryDTO>> getSummary(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success("Notification summary",
                notificationService.getSummary(userId)));
    }

    /**
     * PUT /api/notifications/{id}/read
     * Mark one notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return unauthorized();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    /**
     * PUT /api/notifications/read-all
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return unauthorized();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    /**
     * DELETE /api/notifications/old
     * Delete read notifications older than 30 days
     */
    @DeleteMapping("/old")
    public ResponseEntity<ApiResponse<Void>> deleteOld(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return unauthorized();
        notificationService.deleteOldNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Old notifications deleted", null));
    }

    // ==================== PREFERENCES ====================

    /**
     * GET /api/notifications/preferences
     * Get all notification preferences for the logged-in user
     */
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceDTO>>> getPreferences(
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success("Preferences fetched",
                notificationService.getPreferences(userId)));
    }

    /**
     * PUT /api/notifications/preferences
     * Update a single preference (type + channel + enabled flag)
     */
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDTO>> updatePreference(
            @Valid @RequestBody UpdatePreferenceRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success("Preference updated",
                notificationService.updatePreference(userId, request)));
    }

    /**
     * POST /api/notifications/preferences/reset
     * Reset all preferences back to defaults
     */
    @PostMapping("/preferences/reset")
    public ResponseEntity<ApiResponse<Void>> resetPreferences(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return unauthorized();
        notificationService.initDefaultPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success("Preferences reset to defaults", null));
    }
}
