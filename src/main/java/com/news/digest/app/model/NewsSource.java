package com.news.digest.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        private  long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(nullable = false,length = 500)
    private String url;
    @Column(name = "logo_url",length = 500)
    private String logoUrl ;
    private String category;

    private String language = "en";

    private String country;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "trust_score")
    private Double trustScore = 5.0; // 1-10 scale

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Article> articles = new HashSet<>();

}
