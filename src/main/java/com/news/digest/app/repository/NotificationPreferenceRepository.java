package com.news.digest.app.repository;

import com.news.digest.app.model.NotificationPreference;
import com.news.digest.app.notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository  extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByUserId(Long userId);

    Optional<NotificationPreference> findByUserIdAndNotificationType(Long userId, NotificationType type);

    // All enabled preferences for a type — used by breaking news alert
    // JOIN FETCH user to avoid LazyInitializationException outside a session
    @Query("SELECT p FROM NotificationPreference p JOIN FETCH p.user " +
            "WHERE p.notificationType = :type AND p.isEnabled = true")
    List<NotificationPreference> findEnabledPrefsForType(@Param("type") NotificationType type);

    // Email subscribers only — used by weekly digest scheduler
    @Query("SELECT p FROM NotificationPreference p JOIN FETCH p.user " +
            "WHERE p.notificationType = :type " +
            "AND p.isEnabled = true " +
            "AND (p.channel = 'EMAIL' OR p.channel = 'BOTH')")
    List<NotificationPreference> findEmailSubscribersForType(@Param("type") NotificationType type);

}
