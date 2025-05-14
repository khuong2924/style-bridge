package khuong.com.postingservice.service.impl;

import khuong.com.postingservice.configs.cloudinary.ImageUploadService;
import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.AttachedImage;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import khuong.com.postingservice.repository.AttachedImageRepository;
import khuong.com.postingservice.repository.RecruitmentPostRepository;
import khuong.com.postingservice.service.RecruitmentPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentPostServiceImpl implements RecruitmentPostService {

    private final RecruitmentPostRepository recruitmentPostRepository;
    private final AttachedImageRepository attachedImageRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public RecruitmentPost createPost(RecruitmentPost post, Long userId) {
        post.setPosterUserId(userId);
        
        // Set default status if not provided
        if (post.getStatus() == null) {
            post.setStatus(RecruitmentPostStatus.ACTIVE);
        }
        
        // Set postedAt if not provided
        if (post.getPostedAt() == null) {
            post.setPostedAt(LocalDateTime.now());
        }
        
        return recruitmentPostRepository.save(post);
    }
    
    @Override
    @Transactional
    public List<ImageInfo> addImagesToPost(Long postId, List<MultipartFile> images, Long userId) throws IOException {
        RecruitmentPost post = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to add images to this post"));
        
        List<ImageInfo> uploadedImages = new ArrayList<>();
        int maxOrder = getMaxOrderForPost(post);
        
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                
                AttachedImage attachedImage = AttachedImage.builder()
                        .storagePath(imageUrl)
                        .orderInAlbum(++maxOrder)
                        .recruitmentPost(post)
                        .build();
                
                attachedImage = attachedImageRepository.save(attachedImage);
                
                uploadedImages.add(new ImageInfo(
                        attachedImage.getId(),
                        attachedImage.getStoragePath(),
                        attachedImage.getOrderInAlbum()
                ));
            }
        }
        
        return uploadedImages;
    }
    
    @Override
    @Transactional
    public void deleteImageFromPost(Long postId, Long imageId, Long userId) {
        RecruitmentPost post = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete images from this post"));
        
        AttachedImage image = attachedImageRepository.findByIdAndRecruitmentPostId(imageId, postId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found in this post"));
        
        attachedImageRepository.delete(image);
    }
    
    @Override
    public List<ImageInfo> getPostImages(Long postId) {
        RecruitmentPost post = recruitmentPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        return attachedImageRepository.findByRecruitmentPostOrderByOrderInAlbumAsc(post)
                .stream()
                .map(img -> new ImageInfo(img.getId(), img.getStoragePath(), img.getOrderInAlbum()))
                .collect(Collectors.toList());
    }
    
    private int getMaxOrderForPost(RecruitmentPost post) {
        return attachedImageRepository.findByRecruitmentPostOrderByOrderInAlbumAsc(post)
                .stream()
                .mapToInt(AttachedImage::getOrderInAlbum)
                .max()
                .orElse(0);
    }

    @Override
    @Transactional
    public RecruitmentPost updatePost(Long postId, RecruitmentPost updatedPost, Long userId) {
        RecruitmentPost existingPost = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this post"));

        // Update fields
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setMakeupType(updatedPost.getMakeupType());
        existingPost.setExpectedDuration(updatedPost.getExpectedDuration());
        existingPost.setAddress(updatedPost.getAddress());
        existingPost.setHiringType(updatedPost.getHiringType());
        existingPost.setCompensation(updatedPost.getCompensation());
        existingPost.setQuantity(updatedPost.getQuantity());
        existingPost.setDescription(updatedPost.getDescription());
        
        // Update LocalDateTime fields if provided
        if (updatedPost.getStartTime() != null) {
            existingPost.setStartTime(updatedPost.getStartTime());
        }
        
        if (updatedPost.getDeadline() != null) {
            existingPost.setDeadline(updatedPost.getDeadline());
        }
        
        // Status can be updated if provided
        if (updatedPost.getStatus() != null) {
            existingPost.setStatus(updatedPost.getStatus());
        }

        return recruitmentPostRepository.save(existingPost);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        RecruitmentPost post = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete this post"));
        
        // Delete all attached images first
        attachedImageRepository.deleteByRecruitmentPostId(postId);
        
        // Then delete the post
        recruitmentPostRepository.delete(post);
    }

    @Override
    public Optional<RecruitmentPost> getPostById(Long postId) {
        return recruitmentPostRepository.findByIdWithAttachedImages(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecruitmentPost> getAllPosts(Pageable pageable) {
        log.debug("Getting all posts with pageable: {}", pageable);
        try {
            Page<RecruitmentPost> posts = recruitmentPostRepository.findAllPosts(pageable);
            
            // Initialize the image counts to avoid LazyInitializationException
            for (RecruitmentPost post : posts.getContent()) {
                int imageCount = attachedImageRepository.countByRecruitmentPostId(post.getId());
                // We're not setting the actual images, just making sure the count is available
                if (post.getAttachedImages() == null) {
                    post.setAttachedImages(new ArrayList<>());
                }
            }
            
            return posts;
        } catch (Exception e) {
            log.error("Error fetching posts: {}", e.getMessage(), e);
            // Fallback to simpler query if there are issues
            return recruitmentPostRepository.findAll(pageable);
        }
    }

    @Override
    public Page<RecruitmentPost> getPostsByStatus(RecruitmentPostStatus status, Pageable pageable) {
        return recruitmentPostRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<RecruitmentPost> getPostsByUser(Long userId, Pageable pageable) {
        return recruitmentPostRepository.findByPosterUserId(userId, pageable);
    }

    @Override
    public Page<RecruitmentPost> searchPosts(
            String title,
            String makeupType,
            RecruitmentPostStatus status,
            Pageable pageable) {
        return recruitmentPostRepository.searchPosts(title, makeupType, status, pageable);
    }

    @Override
    public List<RecruitmentPost> getRecentPosts(RecruitmentPostStatus status, int limit) {
        return recruitmentPostRepository.findRecentPosts(status, PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public void updatePostStatus(Long postId, RecruitmentPostStatus status, Long userId) {
        RecruitmentPost post = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this post"));
        
        post.setStatus(status);
        recruitmentPostRepository.save(post);
    }

    @Override
    @Transactional
    public void checkAndUpdateExpiredPosts() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find posts with deadline in the past and status still ACTIVE
        List<RecruitmentPost> expiredPosts = recruitmentPostRepository.findAll().stream()
            .filter(post -> post.getStatus() == RecruitmentPostStatus.ACTIVE 
                    && post.getDeadline() != null 
                    && post.getDeadline().isBefore(now))
            .collect(Collectors.toList());
        
        // Update status to EXPIRED
        for (RecruitmentPost post : expiredPosts) {
            post.setStatus(RecruitmentPostStatus.EXPIRED);
            recruitmentPostRepository.save(post);
            log.info("Updated post ID {} to EXPIRED status", post.getId());
        }
    }
} 