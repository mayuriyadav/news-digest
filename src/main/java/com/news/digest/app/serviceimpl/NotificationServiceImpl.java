package com.news.digest.app.serviceimpl;

import com.news.digest.app.dto.NotificationDTO;
import com.news.digest.app.dto.NotificationPreferenceDTO;
import com.news.digest.app.dto.NotificationSummaryDTO;
import com.news.digest.app.exception.ResourceNotFoundException;
import com.news.digest.app.model.*;
import com.news.digest.app.notification.EmailService;
import com.news.digest.app.notification.NotificationChannel;
import com.news.digest.app.notification.NotificationType;
import com.news.digest.app.notification.UpdatePreferenceRequest;
import com.news.digest.app.repository.*;
import com.news.digest.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl  implements NotificationService {


    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final EmailService emailService;

    // ==================== DELIVERY ====================

    @Override
    @Transactional
    public void sendBreakingNewsAlert(Long articleId, String category) {
        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null || !Boolean.TRUE.equals(article.getIsBreaking())) return;

        // Only notify users who have BREAKING_NEWS enabled — avoids loading all users
        List<NotificationPreference> prefs =
                preferenceRepository.findEnabledPrefsForType(NotificationType.BREAKING_NEWS);

        for (NotificationPreference pref : prefs) {
            User user = pref.getUser();

            // Skip if already notified
            if (notificationRepository.existsByUserIdAndArticleIdAndType(
                    user.getId(), articleId, NotificationType.BREAKING_NEWS)) continue;

            boolean inApp = pref.getChannel() == NotificationChannel.IN_APP
                    || pref.getChannel() == NotificationChannel.BOTH;
            boolean email = pref.getChannel() == NotificationChannel.EMAIL
                    || pref.getChannel() == NotificationChannel.BOTH;

            if (inApp) {
                createInAppNotification(user, NotificationType.BREAKING_NEWS,
                        "Breaking: " + (category != null ? category : "News"),
                        truncate(article.getTitle(), 150),
                        article);
            }
            if (email) {
                sendBreakingNewsEmail(user.getEmail(), article);
            }
        }

        log.info("[Notification] Breaking news alerts sent for article {}", articleId);
    }

    @Override
    @Transactional
    public void sendTrendingAlert(Long articleId) {
        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null) return;

        List<Bookmark> bookmarks = bookmarkRepository.findByArticle_Id(articleId);

        for (Bookmark bookmark : bookmarks) {
            User user = bookmark.getUser();

            if (notificationRepository.existsByUserIdAndArticleIdAndType(
                    user.getId(), articleId, NotificationType.TRENDING_ALERT)) continue;

            Optional<NotificationPreference> pref = preferenceRepository
                    .findByUserIdAndNotificationType(user.getId(), NotificationType.TRENDING_ALERT);

            boolean enabled = pref.map(NotificationPreference::getIsEnabled).orElse(true);
            if (!enabled) continue;

            createInAppNotification(user, NotificationType.TRENDING_ALERT,
                    "Trending: An article you saved is going viral",
                    truncate(article.getTitle(), 150),
                    article);

            boolean emailEnabled = pref.map(p ->
                    p.getChannel() == NotificationChannel.EMAIL ||
                            p.getChannel() == NotificationChannel.BOTH).orElse(false);

            if (emailEnabled) {
                Map<String, Object> vars = new HashMap<>();
                vars.put("userName",           user.getUserName());
                vars.put("articleTitle",        article.getTitle());
                vars.put("articleUrl",          article.getUrl());
                vars.put("articleDescription",  article.getDescription());
                vars.put("viewCount",           article.getViewCount());
                emailService.sendHtml(user.getEmail(),
                        "Trending: " + truncate(article.getTitle(), 60),
                        "trending-alert", vars);
            }
        }
    }

    @Override
    @Transactional
    public void sendCommentReplyAlert(Long commentId, Long parentCommentUserId) {
        User user = userRepository.findById(parentCommentUserId).orElse(null);
        if (user == null) return;

        Optional<NotificationPreference> pref = preferenceRepository
                .findByUserIdAndNotificationType(parentCommentUserId, NotificationType.COMMENT_REPLY);
        if (pref.map(p -> !p.getIsEnabled()).orElse(false)) return;

        createInAppNotification(user, NotificationType.COMMENT_REPLY,
                "Someone replied to your comment",
                "You have a new reply — tap to view",
                null);
    }

    @Override
    public void sendWeeklyDigest() {
        List<NotificationPreference> emailPrefs =
                preferenceRepository.findEmailSubscribersForType(NotificationType.WEEKLY_DIGEST);

        List<Article> topArticles = articleRepository
                .findTrendingArticles(LocalDateTime.now().minusDays(7), PageRequest.of(0, 5))
                .getContent();

        if (topArticles.isEmpty()) {
            log.info("[WeeklyDigest] No articles to send this week");
            return;
        }

        for (NotificationPreference pref : emailPrefs) {
            User user = pref.getUser();
            try {
                Map<String, Object> vars = new HashMap<>();
                vars.put("userName",  user.getUserName());
                vars.put("articles",  topArticles);
                vars.put("weekOf",    LocalDateTime.now().toLocalDate().toString());

                emailService.sendHtml(user.getEmail(), "Your Weekly News Digest",
                        "weekly-digest", vars);
                log.debug("[WeeklyDigest] Sent to {}", user.getEmail());
            } catch (Exception e) {
                log.error("[WeeklyDigest] Failed for {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("[WeeklyDigest] Sent to {} users", emailPrefs.size());
    }

    // ==================== IN-APP INBOX ====================

    @Override
    public Page<NotificationDTO> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @Override
    public Page<NotificationDTO> getUnreadNotifications(Long userId, int page, int size) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @Override
    public NotificationSummaryDTO getSummary(Long userId) {
        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        long total  = notificationRepository.countByUser_Id(userId);
        List<NotificationDTO> recent = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5))
                .map(this::toDTO).getContent();

        return NotificationSummaryDTO.builder()
                .unreadCount(unread)
                .totalCount(total)
                .recent(recent)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        if (updated == 0) throw new ResourceNotFoundException("Notification", "id", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteOldNotifications(Long userId) {
        int deleted = notificationRepository.deleteOldRead(userId, LocalDateTime.now().minusDays(30));
        log.debug("[Notification] Deleted {} old notifications for user {}", deleted, userId);
    }

    // ==================== PREFERENCES ====================

    @Override
    public List<NotificationPreferenceDTO> getPreferences(Long userId) {
        Map<NotificationType, NotificationPreference> byType = preferenceRepository
                .findByUserId(userId).stream()
                .collect(Collectors.toMap(NotificationPreference::getNotificationType, p -> p));

        return Arrays.stream(NotificationType.values())
                .map(type -> {
                    NotificationPreference p = byType.get(type);
                    return NotificationPreferenceDTO.builder()
                            .notificationType(type)
                            .channel(p != null ? p.getChannel() : NotificationChannel.IN_APP)
                            .isEnabled(p != null ? p.getIsEnabled() : Boolean.TRUE)
                            .build();
                })
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public NotificationPreferenceDTO updatePreference(Long userId, UpdatePreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forUserById(userId));

        NotificationPreference pref = preferenceRepository
                .findByUserIdAndNotificationType(userId, request.getNotificationType())
                .orElse(NotificationPreference.builder()
                        .user(user)
                        .notificationType(request.getNotificationType())
                        .build());

        pref.setChannel(request.getChannel());
        pref.setIsEnabled(request.getIsEnabled());
        preferenceRepository.save(pref);

        return NotificationPreferenceDTO.builder()
                .notificationType(pref.getNotificationType())
                .channel(pref.getChannel())
                .isEnabled(pref.getIsEnabled())
                .build();
    }

    @Override
    @Transactional
    public void initDefaultPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forUserById(userId));

        for (NotificationType type : NotificationType.values()) {
            if (preferenceRepository.findByUserIdAndNotificationType(userId, type).isPresent()) continue;

            // WEEKLY_DIGEST defaults to EMAIL so new users get the digest
            // Everything else defaults to IN_APP
            NotificationChannel defaultChannel = (type == NotificationType.WEEKLY_DIGEST)
                    ? NotificationChannel.EMAIL
                    : NotificationChannel.IN_APP;

            preferenceRepository.save(NotificationPreference.builder()
                    .user(user)
                    .notificationType(type)
                    .channel(defaultChannel)
                    .isEnabled(true)
                    .build());
        }
    }

    // ==================== HELPERS ====================

    private void createInAppNotification(User user, NotificationType type,
                                         String title, String message, Article article) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .article(article)
                .articleTitle(article != null ? article.getTitle() : null)
                .articleUrl(article != null ? article.getUrl() : null)
                .isRead(false)
                .build());
    }

    private void sendBreakingNewsEmail(String email, Article article) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("articleTitle",       article.getTitle());
        vars.put("articleDescription", article.getDescription());
        vars.put("articleUrl",         article.getUrl());
        vars.put("articleImageUrl",    article.getImageUrl());
        vars.put("sourceName",         article.getSourceName());
        vars.put("category",           article.getCategory());
        emailService.sendHtml(email,
                "Breaking: " + truncate(article.getTitle(), 60),
                "breaking-news", vars);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .articleId(n.getArticle() != null ? n.getArticle().getId() : null)
                .articleTitle(n.getArticleTitle())
                .articleUrl(n.getArticleUrl())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
