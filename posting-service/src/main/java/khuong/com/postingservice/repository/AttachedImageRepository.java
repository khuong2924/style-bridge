package khuong.com.postingservice.repository;

import khuong.com.postingservice.entity.AttachedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachedImageRepository extends JpaRepository<AttachedImage, Long> {
    
    List<AttachedImage> findByRecruitmentPostId(Long postId);
    
    List<AttachedImage> findByPersonalFeedPostId(Long postId);
    
    @Query("SELECT a FROM AttachedImage a WHERE a.recruitmentPost.posterUserId = :userId")
    List<AttachedImage> findImagesByPosterUserId(@Param("userId") Long userId);
    
    void deleteByRecruitmentPostId(Long postId);
    
    void deleteByPersonalFeedPostId(Long postId);
} 