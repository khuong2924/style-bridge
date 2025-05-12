package khuong.com.postingservice.service.impl;

import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.ApplicationStatus;
import khuong.com.postingservice.repository.ApplicationRequestRepository;
import khuong.com.postingservice.repository.RecruitmentPostRepository;
import khuong.com.postingservice.service.ApplicationRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationRequestServiceImpl implements ApplicationRequestService {

    private final ApplicationRequestRepository applicationRequestRepository;
    private final RecruitmentPostRepository recruitmentPostRepository;

    @Override
    @Transactional
    public ApplicationRequest createApplication(ApplicationRequest application, Long userId) {
        // Check if user has already applied
        Optional<ApplicationRequest> existingApplication = applicationRequestRepository
                .findByRecruitmentPostIdAndApplicantUserId(application.getRecruitmentPost().getId(), userId);
        
        if (existingApplication.isPresent()) {
            throw new IllegalStateException("You have already applied to this post");
        }
        
        // Check if post exists
        RecruitmentPost post = recruitmentPostRepository.findById(application.getRecruitmentPost().getId())
                .orElseThrow(() -> new IllegalArgumentException("Recruitment post not found"));
        
        application.setRecruitmentPost(post);
        application.setApplicantUserId(userId);
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());
        
        return applicationRequestRepository.save(application);
    }

    @Override
    @Transactional
    public ApplicationRequest updateApplication(Long applicationId, ApplicationRequest updatedApplication, Long userId) {
        ApplicationRequest existingApplication = applicationRequestRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        if (!existingApplication.getApplicantUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this application");
        }
        
        existingApplication.setMessage(updatedApplication.getMessage());
        existingApplication.setContactInfo(updatedApplication.getContactInfo());
        
        return applicationRequestRepository.save(existingApplication);
    }

    @Override
    @Transactional
    public void deleteApplication(Long applicationId, Long userId) {
        ApplicationRequest application = applicationRequestRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        if (!application.getApplicantUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this application");
        }
        
        applicationRequestRepository.delete(application);
    }

    @Override
    public Optional<ApplicationRequest> getApplicationById(Long applicationId) {
        return applicationRequestRepository.findById(applicationId);
    }

    @Override
    public Page<ApplicationRequest> getApplicationsByPostId(Long postId, Pageable pageable) {
        return applicationRequestRepository.findByRecruitmentPostId(postId, pageable);
    }

    @Override
    public List<ApplicationRequest> getApplicationsByUserId(Long userId) {
        return applicationRequestRepository.findByApplicantUserId(userId);
    }

    @Override
    public Optional<ApplicationRequest> getApplicationByPostAndUser(Long postId, Long userId) {
        return applicationRequestRepository.findByRecruitmentPostIdAndApplicantUserId(postId, userId);
    }

    @Override
    public Long countApplicationsByPostId(Long postId) {
        return applicationRequestRepository.countApplicationsByPostId(postId);
    }

    @Override
    public Page<ApplicationRequest> getApplicationsForPosterUser(Long userId, Pageable pageable) {
        return applicationRequestRepository.findApplicationsForPosterUser(userId, pageable);
    }

    @Override
    @Transactional
    public void approveApplication(Long applicationId, Long userId) {
        ApplicationRequest application = applicationRequestRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        // Check if the current user is the post owner
        RecruitmentPost post = application.getRecruitmentPost();
        if (!post.getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to approve this application");
        }
        
        application.setStatus(ApplicationStatus.APPROVED);
        application.setProcessedAt(LocalDateTime.now());
        applicationRequestRepository.save(application);
        
        log.info("Application {} has been approved by user {}", applicationId, userId);
    }

    @Override
    @Transactional
    public void rejectApplication(Long applicationId, Long userId) {
        ApplicationRequest application = applicationRequestRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        // Check if the current user is the post owner
        RecruitmentPost post = application.getRecruitmentPost();
        if (!post.getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to reject this application");
        }
        
        application.setStatus(ApplicationStatus.REJECTED);
        application.setProcessedAt(LocalDateTime.now());
        applicationRequestRepository.save(application);
        
        log.info("Application {} has been rejected by user {}", applicationId, userId);
    }
} 