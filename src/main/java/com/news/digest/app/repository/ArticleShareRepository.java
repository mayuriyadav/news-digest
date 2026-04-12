package com.news.digest.app.repository;


import com.news.digest.app.model.ArticleShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleShareRepository extends JpaRepository<ArticleShare, Long> {

    Long countByArticleId(Long articleId);

    @Query("SELECT COUNT(s) FROM ArticleShare s WHERE s.article.id = :articleId AND s.platform = :platform")
    Long countByArticleIdAndPlatform(@Param("articleId") Long articleId, @Param("platform") String platform);


    @Query("SELECT s.platform, COUNT(s) FROM ArticleShare s WHERE s.article.id = :articleId GROUP BY s.platform")
    List<Object[]> getShareCountByPlatform(@Param("articleId") Long articleId);

    @Query("SELECT COUNT(s) FROM ArticleShare s WHERE s.sharedAt BETWEEN :start AND :end")
    Long countSharesInDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}