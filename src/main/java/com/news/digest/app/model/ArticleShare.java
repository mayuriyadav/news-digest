package com.news.digest.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private String platform; // FACEBOOK, TWITTER, WHATSAPP, EMAIL, etc.

    @CreationTimestamp
    @Column(name = "shared_at")
    private LocalDateTime sharedAt;

}
