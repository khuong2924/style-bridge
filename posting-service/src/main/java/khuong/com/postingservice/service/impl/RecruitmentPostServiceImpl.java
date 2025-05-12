package khuong.com.postingservice.service.impl;

import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentPostServiceImpl implements RecruitmentPostService {

    private final RecruitmentPostRepository recruitmentPostRepository;

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