package com.news.digest.app.repository;

import com.news.digest.app.model.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface NewsSourceRepository
        extends JpaRepository<NewsSource, Long> {
    Optional<NewsSource> findByName(String name);
    List<NewsSource> findByCategory(String category);
    List<NewsSource> findByLanguage(String language);
    List<NewsSource> findByCountry(String country);
    List<NewsSource> findByIsActiveTrue();
}
