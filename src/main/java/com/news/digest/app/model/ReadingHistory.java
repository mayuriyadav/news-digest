package com.news.digest.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reading_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @CreationTimestamp
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "read_count")
    private Integer readCount = 1;

    @Column(name = "read_duration_seconds")
    private Integer readDurationSeconds;

    @Column(name = "scroll_depth_percentage")
    private Integer scrollDepthPercentage;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "device_type")
    private String deviceType;

}
