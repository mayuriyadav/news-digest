package com.news.digest.app.repository;

import com.news.digest.app.model.ArticleLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    Optional<ArticleLike> findByUserIdAndArticleId(Long userId, Long articleId);

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ArticleLike al WHERE al.user.id = :userId AND al.article.id = :articleId")
    void deleteByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    Long countByArticleId(Long articleId);

    @Query("SELECT COUNT(al) FROM ArticleLike al WHERE al.article.id = :articleId")
    Long getLikeCountForArticle(@Param("articleId") Long articleId);
}