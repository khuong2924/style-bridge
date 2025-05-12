package khuong.com.postingservice.service;

import khuong.com.postingservice.entity.ApplicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ApplicationRequestService {
    
    ApplicationRequest createApplication(ApplicationRequest application, Long userId);
    
    ApplicationRequest updateApplication(Long applicationId, ApplicationRequest updatedApplication, Long userId);
    
    void deleteApplication(Long applicationId, Long userId);
    
    Optional<ApplicationRequest> getApplicationById(Long applicationId);
    
    Page<ApplicationRequest> getApplicationsByPostId(Long postId, Pageable pageable);
    
    List<ApplicationRequest> getApplicationsByUserId(Long userId);
    
    Optional<ApplicationRequest> getApplicationByPostAndUser(Long postId, Long userId);
    
    Long countApplicationsByPostId(Long postId);
    
    Page<ApplicationRequest> getApplicationsForPosterUser(Long userId, Pageable pageable);
    
    void approveApplication(Long applicationId, Long userId);
    
    void rejectApplication(Long applicationId, Long userId);
} 