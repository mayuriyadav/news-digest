package com.news.digest.app.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.news.digest.app.model.Notification;
import com.news.digest.app.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc( Long userId ,Pageable pageable);

  long countByUserIdAndIsReadFalse(Long userId);
  long countByUser_Id(Long userId);


    // Mark single notification as read — only owner can mark their own
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now " +
            "WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id,
                   @Param("userId") Long userId,
                   @Param("now") LocalDateTime now);

    // Mark all as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now " +
            "WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Delete old read notifications for one user
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n " +
            "WHERE n.user.id = :userId AND n.isRead = true AND n.readAt < :before")
    int deleteOldRead(@Param("userId") Long userId, @Param("before") LocalDateTime before);

    // Batch-delete old read notifications across ALL users (for nightly cleanup job)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :before")
    int deleteAllOldRead(@Param("before") LocalDateTime before);

    // Dedup guard for breaking news — don't send same article twice to same user
    boolean existsByUserIdAndArticleIdAndType(Long userId, Long articleId, NotificationType type);


}
