package khuong.com.postingservice.repository;

import khuong.com.postingservice.entity.AttachedImage;
import khuong.com.postingservice.entity.RecruitmentPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachedImageRepository extends JpaRepository<AttachedImage, Long> {
    
    List<AttachedImage> findByRecruitmentPostOrderByOrderInAlbumAsc(RecruitmentPost recruitmentPost);
    
    Optional<AttachedImage> findByIdAndRecruitmentPostId(Long id, Long recruitmentPostId);
    
    void deleteByRecruitmentPostId(Long recruitmentPostId);
    
    List<AttachedImage> findByApplicationRequestIdOrderByOrderInAlbumAsc(Long applicationRequestId);
    
    List<AttachedImage> findByPersonalFeedPostIdOrderByOrderInAlbumAsc(Long personalFeedPostId);
    
    int countByRecruitmentPostId(Long recruitmentPostId);
} 