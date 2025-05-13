package khuong.com.postingservice.controller;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.ApplicationRequest;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<ApplicationRequest> getApplicationById(@PathVariable Long applicationId) {
        return applicationRequestService.getApplicationById(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationRequest>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getAllApplications(pageable);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<ApplicationRequest>> getApplicationsByUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getApplicationsByUser(userId, pageable);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<ApplicationRequest>> getApplicationsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getApplicationsByPost(postId, userId, pageable);
        return ResponseEntity.ok(applications);
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
} 