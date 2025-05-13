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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
        post.setStatus(RecruitmentPostStatus.ACTIVE);
        post.setPostedAt(LocalDateTime.now());
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
    @CacheEvict(value = "recruitmentPosts", key = "#postId")
    public RecruitmentPost updatePost(Long postId, RecruitmentPost updatedPost, Long userId) {
        RecruitmentPost existingPost = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this post"));

        // Update fields
        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setMakeupType(updatedPost.getMakeupType());
        existingPost.setStartTime(updatedPost.getStartTime());
        existingPost.setExpectedDuration(updatedPost.getExpectedDuration());
        existingPost.setAddress(updatedPost.getAddress());
        existingPost.setHiringType(updatedPost.getHiringType());
        existingPost.setCompensation(updatedPost.getCompensation());
        existingPost.setQuantity(updatedPost.getQuantity());
        existingPost.setDescription(updatedPost.getDescription());
        existingPost.setDeadline(updatedPost.getDeadline());

        return recruitmentPostRepository.save(existingPost);
    }

    @Override
    @Transactional
    @CacheEvict(value = "recruitmentPosts", key = "#postId")
    public void deletePost(Long postId, Long userId) {
        RecruitmentPost post = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete this post"));
        
        // Delete all attached images first
        attachedImageRepository.deleteByRecruitmentPostId(postId);
        
        // Then delete the post
        recruitmentPostRepository.delete(post);
    }

    @Override
    @Cacheable(value = "recruitmentPosts", key = "#postId")
    public Optional<RecruitmentPost> getPostById(Long postId) {
        return recruitmentPostRepository.findById(postId);
    }

    @Override
    public Page<RecruitmentPost> getAllPosts(Pageable pageable) {
        return recruitmentPostRepository.findAll(pageable);
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
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return recruitmentPostRepository.searchPosts(title, makeupType, status, startDate, endDate, pageable);
    }

    @Override
    public List<RecruitmentPost> getRecentPosts(RecruitmentPostStatus status, int limit) {
        return recruitmentPostRepository.findRecentPosts(status, PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    @CacheEvict(value = "recruitmentPosts", key = "#postId")
    public void updatePostStatus(Long postId, RecruitmentPostStatus status, Long userId) {
        RecruitmentPost post = recruitmentPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this post"));
        
        post.setStatus(status);
        recruitmentPostRepository.save(post);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void checkAndUpdateExpiredPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<RecruitmentPost> expiredPosts = recruitmentPostRepository.findByDeadlineBefore(now);
        
        for (RecruitmentPost post : expiredPosts) {
            if (post.getStatus() == RecruitmentPostStatus.ACTIVE) {
                post.setStatus(RecruitmentPostStatus.EXPIRED);
                recruitmentPostRepository.save(post);
                log.info("Post with ID {} has been marked as expired", post.getId());
            }
        }
    }
} 