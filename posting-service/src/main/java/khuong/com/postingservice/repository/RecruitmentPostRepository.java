package khuong.com.postingservice.repository;

import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentPostRepository extends JpaRepository<RecruitmentPost, Long> {
    
    Page<RecruitmentPost> findByStatus(RecruitmentPostStatus status, Pageable pageable);
    
    Page<RecruitmentPost> findByPosterUserId(Long userId, Pageable pageable);
    
    @Query("SELECT r FROM RecruitmentPost r WHERE " +
           "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:makeupType IS NULL OR LOWER(r.makeupType) LIKE LOWER(CONCAT('%', :makeupType, '%'))) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<RecruitmentPost> searchPosts(
            @Param("title") String title,
            @Param("makeupType") String makeupType,
            @Param("status") RecruitmentPostStatus status,
            Pageable pageable);
    
    @Query("SELECT r FROM RecruitmentPost r WHERE r.status = :status")
    List<RecruitmentPost> findRecentPosts(@Param("status") RecruitmentPostStatus status, Pageable pageable);
    
    Optional<RecruitmentPost> findByIdAndPosterUserId(Long id, Long userId);
    
    @Query("SELECT r FROM RecruitmentPost r LEFT JOIN FETCH r.attachedImages WHERE r.id = :id")
    Optional<RecruitmentPost> findByIdWithAttachedImages(@Param("id") Long id);
    
    @Query(value = "SELECT DISTINCT r FROM RecruitmentPost r",
           countQuery = "SELECT COUNT(r) FROM RecruitmentPost r")
    Page<RecruitmentPost> findAllPosts(Pageable pageable);
} 