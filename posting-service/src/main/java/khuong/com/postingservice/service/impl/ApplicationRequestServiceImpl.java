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
        
        return applicationRequestRepository.save(application);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImageInfo> addImagesToApplication(Long applicationId, List<MultipartFile> images, Long userId) throws IOException {
        // First, validate permissions before starting any operations
        ApplicationRequest application = applicationRequestRepository.findByIdAndApplicantUserId(applicationId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to add images to this application"));
        
        List<ImageInfo> uploadedImages = new ArrayList<>();
        int maxOrder = getMaxOrderForApplication(applicationId);
        
        // Process each image in a separate try-catch to allow partial success
        for (MultipartFile image : images) {
            try {
                if (image == null || image.isEmpty()) {
                    log.warn("Skipping empty image file");
                    continue;
                }
                
                // First upload the image to external storage (Cloudinary)
                String imageUrl = imageUploadService.uploadImage(image);
                log.info("Image uploaded successfully to: {}", imageUrl);
                
                // Then save the metadata to our database
                AttachedImage attachedImage = AttachedImage.builder()
                        .storagePath(imageUrl)
                        .orderInAlbum(++maxOrder)
                        .applicationRequest(application)
                        .build();
                
                attachedImage = attachedImageRepository.save(attachedImage);
                log.info("Image metadata saved with ID: {}", attachedImage.getId());
                
                uploadedImages.add(new ImageInfo(
                        attachedImage.getId(),
                        attachedImage.getStoragePath(),
                        attachedImage.getOrderInAlbum()
                ));
            } catch (Exception e) {
                log.error("Error processing image: {}", e.getMessage(), e);
                // We don't throw here to allow other images to be processed
            }
        }
        
        // We need at least one successful image upload, otherwise throw an exception
        if (images.size() > 0 && uploadedImages.isEmpty()) {
            throw new IOException("Failed to upload any images. All uploads failed.");
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
    public Page<ApplicationRequest> getApplicationsByPost(Long postId, Pageable pageable) {
        // No authentication check - available to anyone
        return applicationRequestRepository.findByRecruitmentPostId(postId, pageable);
    }

    @Override
    public Long countApplicationsByPost(Long postId) {
        return applicationRequestRepository.countApplicationsByPostId(postId);
    }

    @Override
    public Page<ApplicationRequest> getApplicationsForPosterUser(Long posterId, Pageable pageable) {
        // Use the repository method that finds applications where the user is the post author
        return applicationRequestRepository.findApplicationsForPosterUser(posterId, pageable);
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
        
        return applicationRequestRepository.save(application);
    }
}