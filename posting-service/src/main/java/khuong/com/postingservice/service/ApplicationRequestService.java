package khuong.com.postingservice.service;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ApplicationRequestService {
    
    ApplicationRequest createApplication(ApplicationRequest application, Long userId);
    
    ApplicationRequest updateApplication(Long applicationId, ApplicationRequest updatedApplication, Long userId);
    
    void deleteApplication(Long applicationId, Long userId);
    
    Optional<ApplicationRequest> getApplicationById(Long applicationId);
    
    Page<ApplicationRequest> getAllApplications(Pageable pageable);
    
    Page<ApplicationRequest> getApplicationsByUser(Long userId, Pageable pageable);
    
    Page<ApplicationRequest> getApplicationsByPost(Long postId, Long userId, Pageable pageable);
    
    ApplicationRequest updateApplicationStatus(Long applicationId, ApplicationStatus status, Long userId);
    
    List<ImageInfo> addImagesToApplication(Long applicationId, List<MultipartFile> images, Long userId) throws IOException;
    
    void deleteImageFromApplication(Long applicationId, Long imageId, Long userId);
    
    List<ImageInfo> getApplicationImages(Long applicationId);
} 