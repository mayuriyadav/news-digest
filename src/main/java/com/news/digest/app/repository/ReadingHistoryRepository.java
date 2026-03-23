package com.news.digest.app.repository;

import com.news.digest.app.model.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReadingHistoryRepository  extends JpaRepository<ReadingHistory, Long> {

    Optional<ReadingHistory> findByUserIdAndArticleId(Long userId, Long articleId);

    Page<ReadingHistory> findByUserIdOrderByReadAtDesc(Long userId, Pageable pageable);

    @Query("SELECT rh FROM ReadingHistory rh WHERE rh.user.id = :userId ORDER BY rh.readAt DESC")
    List<ReadingHistory> findRecentReads(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(rh) FROM ReadingHistory rh WHERE rh.user.id = :userId")
    Long countTotalReadsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT rh.article.id) FROM ReadingHistory rh WHERE rh.user.id = :userId")
    Long countUniqueArticlesReadByUser(@Param("userId") Long userId);

    @Query("SELECT AVG(rh.readDurationSeconds) FROM ReadingHistory rh WHERE rh.user.id = :userId AND rh.readDurationSeconds IS NOT NULL")
    Double getAverageReadTimeByUser(@Param("userId") Long userId);

    @Query("SELECT rh.article.category, COUNT(rh) FROM ReadingHistory rh " +
            "WHERE rh.user.id = :userId GROUP BY rh.article.category ORDER BY COUNT(rh) DESC")
    List<Object[]> getReadingPreferences(@Param("userId") Long userId);

    @Query("SELECT DATE(rh.readAt), COUNT(rh) FROM ReadingHistory rh " +
            "WHERE rh.user.id = :userId AND rh.readAt BETWEEN :start AND :end " +
            "GROUP BY DATE(rh.readAt) ORDER BY DATE(rh.readAt)")
    List<Object[]> getReadingActivityByDate(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(rh.readCount) FROM ReadingHistory rh WHERE rh.article.id = :articleId")
    Long getTotalReadsForArticle(@Param("articleId") Long articleId);

    @Query("SELECT COUNT(DISTINCT rh.user.id) FROM ReadingHistory rh WHERE rh.article.id = :articleId")
    Long getUniqueReadersForArticle(@Param("articleId") Long articleId);
}


