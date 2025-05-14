package khuong.com.postingservice.controller;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.dto.ApplicationRequestDTO;
import khuong.com.postingservice.dto.ApplicationRequestListDTO;
import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.ApplicationStatus;
import khuong.com.postingservice.service.ApplicationRequestService;
import khuong.com.postingservice.utils.TokenExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationRequestController {

    private final ApplicationRequestService applicationRequestService;
    private final TokenExtractor tokenExtractor;

    @PostMapping
    public ResponseEntity<ApplicationRequest> createApplication(
            @Valid @RequestBody ApplicationRequest application,
            Authentication authentication) {
        try {
            Long userId = tokenExtractor.extractUserId(authentication);
            ApplicationRequest createdApplication = applicationRequestService.createApplication(application, userId);
            return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping(value = "/{applicationId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageInfo>> uploadImages(
            @PathVariable Long applicationId,
            @RequestParam("images") List<MultipartFile> images,
            Authentication authentication) {
        try {
            Long userId = tokenExtractor.extractUserId(authentication);
            
            List<ImageInfo> uploadedImages = applicationRequestService.addImagesToApplication(applicationId, images, userId);
            return ResponseEntity.ok(uploadedImages);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IOException e) {
            log.error("Error uploading images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{applicationId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long applicationId,
            @PathVariable Long imageId,
            Authentication authentication) {
        try {
            Long userId = tokenExtractor.extractUserId(authentication);
            applicationRequestService.deleteImageFromApplication(applicationId, imageId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @GetMapping("/{applicationId}/images")
    public ResponseEntity<List<ImageInfo>> getApplicationImages(
            @PathVariable Long applicationId) {
        List<ImageInfo> images = applicationRequestService.getApplicationImages(applicationId);
        return ResponseEntity.ok(images);
    }

    @PutMapping("/{applicationId}")
    public ResponseEntity<ApplicationRequest> updateApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationRequest application,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        ApplicationRequest updatedApplication = applicationRequestService.updateApplication(applicationId, application, userId);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        applicationRequestService.deleteApplication(applicationId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationRequestDTO> getApplicationById(@PathVariable Long applicationId) {
        return applicationRequestService.getApplicationById(applicationId)
                .map(application -> {
                    ApplicationRequestDTO dto = ApplicationRequestDTO.fromEntity(application);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ApplicationRequestListDTO>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getAllApplications(pageable);
        // Convert to lightweight DTO page
        Page<ApplicationRequestListDTO> applicationDTOs = applications.map(ApplicationRequestListDTO::fromEntity);
        return ResponseEntity.ok(applicationDTOs);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getApplicationsByUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // If not authenticated, return empty result with a message
        if (authentication == null) {
            return ResponseEntity.ok(
                Map.of(
                    "content", new ArrayList<>(),
                    "message", "Login to view your applications"
                )
            );
        }
        
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getApplicationsByUser(userId, pageable);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getApplicationsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            // For public access without authentication
            if (authentication == null) {
                // Return public information like count
                Long count = applicationRequestService.countApplicationsByPost(postId);
                return ResponseEntity.ok(Map.of(
                    "postId", postId,
                    "applicationCount", count
                ));
            }
            
            // For authenticated users
            Long userId = Long.valueOf(authentication.getPrincipal().toString());
            Page<ApplicationRequest> applications = applicationRequestService.getApplicationsByPost(postId, pageable);
            Page<ApplicationRequestDTO> applicationDTOs = applications.map(ApplicationRequestDTO::fromEntity);
            return ResponseEntity.ok(applicationDTOs);
        } catch (Exception e) {
            log.error("Error retrieving applications for post {}: {}", postId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while retrieving applications"));
        }
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationRequest> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        ApplicationRequest updatedApplication = applicationRequestService.updateApplicationStatus(applicationId, status, userId);
        return ResponseEntity.ok(updatedApplication);
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createApplicationWithImages(
            @RequestParam("recruitmentPostId") Long recruitmentPostId,
            @RequestParam("message") String message,
            @RequestParam(value = "otherSkills", required = false) String otherSkills,
            @RequestParam(value = "preferredContactMethod", required = false) String preferredContactMethod, 
            @RequestParam(value = "availability", required = false) String availability,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        
        try {
            log.info("Creating application for post ID: {}", recruitmentPostId);
            
            // Xác thực người dùng với xử lý lỗi rõ ràng
            if (authentication == null) {
                log.error("Authentication object is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
            }
            
            Long userId;
            try {
                userId = tokenExtractor.extractUserId(authentication);
                log.info("User ID extracted from authentication: {}", userId);
            } catch (Exception e) {
                log.error("Failed to extract user ID from authentication: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid authentication token"));
            }
            
            // Tạo đối tượng ApplicationRequest
            ApplicationRequest application = new ApplicationRequest();
            RecruitmentPost post = new RecruitmentPost();
            post.setId(recruitmentPostId);
            application.setRecruitmentPost(post);
            application.setMessage(message);
            application.setContactInfo(String.format("Kỹ năng: %s, Liên hệ: %s, Thời gian: %s", 
                otherSkills, preferredContactMethod, availability));
            application.setStatus(ApplicationStatus.PENDING);
            
            // Lưu yêu cầu ứng tuyển
            ApplicationRequest createdApplication;
            try {
                createdApplication = applicationRequestService.createApplication(application, userId);
                log.info("Application created with ID: {}", createdApplication.getId());
            } catch (Exception e) {
                log.error("Error creating application: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create application: " + e.getMessage()));
            }
            
            // Upload images nếu có
            List<ImageInfo> uploadedImages = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                log.info("Uploading {} portfolio images", images.size());
                try {
                    // Validate images before upload
                    for (MultipartFile image : images) {
                        if (image.isEmpty()) {
                            continue;
                        }
                        
                        // Check file type
                        String contentType = image.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                            log.warn("Skipping non-image file: {}, type: {}", image.getOriginalFilename(), contentType);
                            continue;
                        }
                        
                        log.debug("Processing image: {}, size: {}", image.getOriginalFilename(), image.getSize());
                    }
                    
                    uploadedImages = applicationRequestService.addImagesToApplication(
                        createdApplication.getId(), images, userId);
                    log.info("Successfully uploaded {} portfolio images", uploadedImages.size());
                } catch (IOException e) {
                    log.error("Error uploading portfolio images: {}", e.getMessage(), e);
                    // Tiếp tục với việc tạo application ngay cả khi upload ảnh thất bại
                }
            } else {
                log.info("No portfolio images provided");
            }
            
            // Tạo response với dữ liệu đã được chọn lọc
            Map<String, Object> response = new HashMap<>();
            response.put("id", createdApplication.getId());
            response.put("recruitmentPostId", createdApplication.getRecruitmentPost() != null ? 
                createdApplication.getRecruitmentPost().getId() : null);
            response.put("message", "Application created successfully");
            response.put("status", createdApplication.getStatus().toString());
            
            // Thêm thông tin hình ảnh đã upload
            if (!uploadedImages.isEmpty()) {
                List<Map<String, Object>> imagesList = uploadedImages.stream()
                    .map(img -> {
                        Map<String, Object> imgMap = new HashMap<>();
                        imgMap.put("id", img.getId());
                        imgMap.put("url", img.getStoragePath());
                        imgMap.put("order", img.getOrderInAlbum());
                        return imgMap;
                    })
                    .collect(Collectors.toList());
                response.put("images", imagesList);
            }
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Unexpected error creating application with images: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}