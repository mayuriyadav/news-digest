package com.news.digest.app.notification;

public enum NotificationType {

    BREAKING_NEWS,       // breaking article ingested in user's followed category
    TRENDING_ALERT,      // article user bookmarked is now trending
    COMMENT_REPLY,       // someone replied to user's comment
    ARTICLE_LIKED,       // someone liked user's comment
    WEEKLY_DIGEST,       // weekly summary email
    SYSTEM               // platform announcements
}
