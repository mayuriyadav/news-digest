package com.news.digest.app.notification;

import com.news.digest.app.repository.NotificationRepository;
import com.news.digest.app.service.NotificationService;
import com.news.digest.app.service.impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {


    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // Every Sunday at 8am — weekly digest email to all subscribed users
    @Scheduled(cron = "${notification.weekly-digest.cron:0 0 8 * * SUN}")
    public void sendWeeklyDigests() {
        log.info("[NotificationScheduler] Sending weekly digests...");
        notificationService.sendWeeklyDigest();
    }

    // Every day at midnight — batch-delete read notifications older than 30 days
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteAllOldRead(cutoff);
        log.info("[NotificationScheduler] Cleaned {} old notifications (read before {})", deleted, cutoff.toLocalDate());
    }
}
