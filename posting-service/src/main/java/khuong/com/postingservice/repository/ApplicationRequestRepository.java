package khuong.com.postingservice.repository;

import khuong.com.postingservice.entity.ApplicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRequestRepository extends JpaRepository<ApplicationRequest, Long> {
    
    Page<ApplicationRequest> findByRecruitmentPostId(Long postId, Pageable pageable);
    
    List<ApplicationRequest> findByApplicantUserId(Long userId);
    
    Optional<ApplicationRequest> findByRecruitmentPostIdAndApplicantUserId(Long postId, Long userId);
    
    @Query("SELECT COUNT(a) FROM ApplicationRequest a WHERE a.recruitmentPost.id = :postId")
    Long countApplicationsByPostId(@Param("postId") Long postId);
    
    @Query("SELECT a FROM ApplicationRequest a WHERE a.recruitmentPost.posterUserId = :userId")
    Page<ApplicationRequest> findApplicationsForPosterUser(@Param("userId") Long userId, Pageable pageable);
} 