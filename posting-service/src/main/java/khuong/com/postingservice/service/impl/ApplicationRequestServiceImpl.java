package khuong.com.postingservice.service.impl;

import khuong.com.postingservice.configs.cloudinary.ImageUploadService;
import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.entity.AttachedImage;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.ApplicationStatus;
import khuong.com.postingservice.repository.ApplicationRequestRepository;
import khuong.com.postingservice.repository.AttachedImageRepository;
import khuong.com.postingservice.repository.RecruitmentPostRepository;
import khuong.com.postingservice.service.ApplicationRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
public class ApplicationRequestServiceImpl implements ApplicationRequestService {

    private final ApplicationRequestRepository applicationRequestRepository;
    private final RecruitmentPostRepository recruitmentPostRepository;
    private final AttachedImageRepository attachedImageRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public ApplicationRequest createApplication(ApplicationRequest application, Long userId) {
        // Verify the post exists
        RecruitmentPost post = recruitmentPostRepository.findById(application.getRecruitmentPost().getId())
                .orElseThrow(() -> new IllegalArgumentException("Recruitment post not found"));
        
        // Check if user already applied
        if (applicationRequestRepository.existsByRecruitmentPostIdAndApplicantUserId(
                post.getId(), userId)) {
            throw new IllegalStateException("You have already applied to this post");
        }
        
        application.setApplicantUserId(userId);
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());
        
        return applicationRequestRepository.save(application);
    }
    
    @Override
    @Transactional
    public List<ImageInfo> addImagesToApplication(Long applicationId, List<MultipartFile> images, Long userId) throws IOException {
        ApplicationRequest application = applicationRequestRepository.findByIdAndApplicantUserId(applicationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to add images to this application"));
        
        List<ImageInfo> uploadedImages = new ArrayList<>();
        int maxOrder = getMaxOrderForApplication(applicationId);
        
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                
                AttachedImage attachedImage = AttachedImage.builder()
                        .storagePath(imageUrl)
                        .orderInAlbum(++maxOrder)
                        .applicationRequest(application)
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
    public void deleteImageFromApplication(Long applicationId, Long imageId, Long userId) {
        // Check if the application belongs to the user
        ApplicationRequest application = applicationRequestRepository.findByIdAndApplicantUserId(applicationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete images from this application"));
        
        // Find the image
        AttachedImage image = attachedImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        
        // Check if the image belongs to this application
        if (image.getApplicationRequest() == null || !image.getApplicationRequest().getId().equals(applicationId)) {
            throw new IllegalArgumentException("Image does not belong to this application");
        }
        
        attachedImageRepository.delete(image);
    }
    
    @Override
    public List<ImageInfo> getApplicationImages(Long applicationId) {
        return attachedImageRepository.findByApplicationRequestIdOrderByOrderInAlbumAsc(applicationId)
                .stream()
                .map(img -> new ImageInfo(img.getId(), img.getStoragePath(), img.getOrderInAlbum()))
                .collect(Collectors.toList());
    }
    
    private int getMaxOrderForApplication(Long applicationId) {
        return attachedImageRepository.findByApplicationRequestIdOrderByOrderInAlbumAsc(applicationId)
                .stream()
                .mapToInt(AttachedImage::getOrderInAlbum)
                .max()
                .orElse(0);
    }

    @Override
    @Transactional
    public ApplicationRequest updateApplication(Long applicationId, ApplicationRequest updatedApplication, Long userId) {
        ApplicationRequest existingApplication = applicationRequestRepository.findByIdAndApplicantUserId(applicationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this application"));
        
        // Only allow updates if the application is still pending
        if (existingApplication.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Cannot update application that is not in PENDING state");
        }
        
        existingApplication.setMessage(updatedApplication.getMessage());
        existingApplication.setContactInfo(updatedApplication.getContactInfo());
        
        return applicationRequestRepository.save(existingApplication);
    }

    @Override
    @Transactional
    public void deleteApplication(Long applicationId, Long userId) {
        ApplicationRequest application = applicationRequestRepository.findByIdAndApplicantUserId(applicationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete this application"));
        
        // Only allow deletion if the application is still pending
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Cannot delete application that is not in PENDING state");
        }
        
        applicationRequestRepository.delete(application);
    }

    @Override
    public Optional<ApplicationRequest> getApplicationById(Long applicationId) {
        return applicationRequestRepository.findById(applicationId);
    }

    @Override
    public Page<ApplicationRequest> getAllApplications(Pageable pageable) {
        return applicationRequestRepository.findAll(pageable);
    }

    @Override
    public Page<ApplicationRequest> getApplicationsByUser(Long userId, Pageable pageable) {
        return applicationRequestRepository.findByApplicantUserId(userId, pageable);
    }

    @Override
    public Page<ApplicationRequest> getApplicationsByPost(Long postId, Long userId, Pageable pageable) {
        // Check if the user is the poster of the recruitment post
        RecruitmentPost post = recruitmentPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Recruitment post not found"));
        
        if (!post.getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to view applications for this post");
        }
        
        return applicationRequestRepository.findByRecruitmentPostId(postId, pageable);
    }

    @Override
    @Transactional
    public ApplicationRequest updateApplicationStatus(Long applicationId, ApplicationStatus status, Long userId) {
        // Get the application
        ApplicationRequest application = applicationRequestRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        // Check if the user is the poster of the recruitment post
        RecruitmentPost post = application.getRecruitmentPost();
        if (!post.getPosterUserId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to update this application status");
        }
        
        // Update the status
        application.setStatus(status);
        application.setProcessedAt(LocalDateTime.now());
        
        return applicationRequestRepository.save(application);
    }
} 