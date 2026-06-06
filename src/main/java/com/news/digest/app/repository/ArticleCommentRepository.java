package com.news.digest.app.repository;

import com.news.digest.app.model.ArticleComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {


    // Top-level comments for an article (no parent)
    Page<ArticleComment> findByArticleIdAndParentIsNullOrderByCreatedAtDesc(Long articleId, Pageable pageable);

    // Replies to a specific comment
    Page<ArticleComment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    // All comments by a user
    Page<ArticleComment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Total comment count for an article
    Long countByArticleId(Long articleId);

    // Increment like count on a comment
    @Modifying
    @Transactional
    @Query("UPDATE ArticleComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

}


