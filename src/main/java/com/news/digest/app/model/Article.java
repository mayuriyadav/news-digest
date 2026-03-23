package com.news.digest.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false, unique = true, length = 500)
    private String url;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_caption", length = 500)
    private String imageCaption;

    @Column(length = 200)
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    @JsonIgnore
    private NewsSource source;
    @Column(name = "source_id" , insertable = false, updatable = false)
    private Long sourceId;
    @Column(name = "source_name")
    private String sourceName; // Denormalized field for quick access

    @Column(name = "source_url")
    private String sourceUrl; // Denormalized field for quick access

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    // Categorization
    private String category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    private String language = "en";

    private String country;

    // Engagement Metrics
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "bookmark_count")
    private Integer bookmarkCount = 0;

    @Column(name = "share_count")
    private Integer shareCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    // Readability
    @Column(name = "read_time_minutes")
    private Integer readTimeMinutes;

    @Column(name = "word_count")
    private Integer wordCount;

    // Content Analysis
    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1.0 to 1.0

    @Column(name = "sentiment_label")
    private String sentimentLabel; // POSITIVE, NEGATIVE, NEUTRAL

    @Column(name = "readability_score")
    private Double readabilityScore; // Flesch-Kincaid score

    // Metadata
    private String featuredImage;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_breaking")
    private Boolean isBreaking = false;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Bookmark> bookmarks = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ReadingHistory> readingHistory = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ArticleLike> likes = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ArticleComment> comments = new HashSet<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ArticleShare> shares = new HashSet<>();

    // Helper Methods
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1 : this.viewCount + 1;
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null) ? 1 : this.likeCount + 1;
    }

    public void decrementLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementBookmarkCount() {
        this.bookmarkCount = (this.bookmarkCount == null) ? 1 : this.bookmarkCount + 1;
    }

    public void decrementBookmarkCount() {
        if (this.bookmarkCount != null && this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }

    public void incrementShareCount() {
        this.shareCount = (this.shareCount == null) ? 1 : this.shareCount + 1;
    }

    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null) ? 1 : this.commentCount + 1;
    }

    public void decrementCommentCount() {
        if (this.commentCount != null && this.commentCount > 0) {
            this.commentCount--;
        }
    }

    // Calculate word count from content
    public void calculateWordCount() {
        if (this.content != null && !this.content.isEmpty()) {
            this.wordCount = this.content.split("\\s+").length;
        } else {
            this.wordCount = 0;
        }
    }

    // Calculate read time based on word count (average reading speed: 200 words/minute)
    public void calculateReadTime() {
        if (this.wordCount == null) {
            calculateWordCount();
        }
        this.readTimeMinutes = (int) Math.ceil(this.wordCount / 200.0);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.viewCount == null) this.viewCount = 0;
        if (this.likeCount == null) this.likeCount = 0;
        if (this.bookmarkCount == null) this.bookmarkCount = 0;
        if (this.shareCount == null) this.shareCount = 0;
        if (this.commentCount == null) this.commentCount = 0;
        calculateWordCount();
        calculateReadTime();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.content != null) {
            calculateWordCount();
            calculateReadTime();
        }
    }
}
