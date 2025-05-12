package khuong.com.postingservice.repository;

import khuong.com.postingservice.entity.PersonalFeedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalFeedPostRepository extends JpaRepository<PersonalFeedPost, Long> {
    
    Page<PersonalFeedPost> findByPosterUserId(Long userId, Pageable pageable);
    
    @Query("SELECT p FROM PersonalFeedPost p ORDER BY p.postedAt DESC")
    Page<PersonalFeedPost> findRecentPosts(Pageable pageable);
    
    @Query("SELECT p FROM PersonalFeedPost p WHERE " +
           "LOWER(p.caption) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PersonalFeedPost> searchPosts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p.tags FROM PersonalFeedPost p WHERE p.tags IS NOT NULL")
    List<String> findAllTags();
} 