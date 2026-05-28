package com.news.digest.app.repository;

import com.news.digest.app.model.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // Find by user and article
    Optional<Bookmark> findByUserIdAndArticleId(Long userId, Long articleId);

    // Check existence
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    // Delete by user and article
    @Modifying
    @Transactional
    @Query("DELETE FROM Bookmark b WHERE b.user.id = :userId AND b.article.id = :articleId")
    void deleteByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    // Get all bookmarks for a user
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Filter bookmarks by folder
    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.folder = :folder ORDER BY b.createdAt DESC")
    Page<Bookmark> findByUserIdAndFolder(@Param("userId") Long userId, @Param("folder") String folder, Pageable pageable);


    // Count by user
    Long countByUserId(Long userId);

    // Count by article
    Long countByArticleId(Long articleId);

    // Get distinct folders for a user
    @Query("SELECT DISTINCT b.folder FROM Bookmark b WHERE b.user.id = :userId")
    List<String> findDistinctFoldersByUserId(@Param("userId") Long userId);

    List<Bookmark> findByArticle_Id(Long articleId);

    @Query("SELECT b.article.id FROM Bookmark b WHERE b.user.id = :userId AND b.article.id IN :articleIds")
    Set<Long> findBookmarkedArticleIds(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);



}
