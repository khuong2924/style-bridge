package khuong.com.postingservice.service;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecruitmentPostService {
    
    RecruitmentPost createPost(RecruitmentPost post, Long userId);
    
    RecruitmentPost updatePost(Long postId, RecruitmentPost updatedPost, Long userId);
    
    void deletePost(Long postId, Long userId);
    
    Optional<RecruitmentPost> getPostById(Long postId);
    
    Page<RecruitmentPost> getAllPosts(Pageable pageable);
    
    Page<RecruitmentPost> getPostsByStatus(RecruitmentPostStatus status, Pageable pageable);
    
    Page<RecruitmentPost> getPostsByUser(Long userId, Pageable pageable);
    
    Page<RecruitmentPost> searchPosts(
            String title,
            String makeupType,
            RecruitmentPostStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
    
    List<RecruitmentPost> getRecentPosts(RecruitmentPostStatus status, int limit);
    
    void updatePostStatus(Long postId, RecruitmentPostStatus status, Long userId);
    
    void checkAndUpdateExpiredPosts();
    
    List<ImageInfo> addImagesToPost(Long postId, List<MultipartFile> images, Long userId) throws IOException;
    
    void deleteImageFromPost(Long postId, Long imageId, Long userId);
    
    List<ImageInfo> getPostImages(Long postId);
} 