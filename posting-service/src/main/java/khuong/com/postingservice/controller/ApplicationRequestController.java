package khuong.com.postingservice.controller;

import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.service.ApplicationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationRequestController {

    private final ApplicationRequestService applicationRequestService;

    @PostMapping
    public ResponseEntity<ApplicationRequest> createApplication(
            @Valid @RequestBody ApplicationRequest application,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        ApplicationRequest createdApplication = applicationRequestService.createApplication(application, userId);
        return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
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

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<ApplicationRequest>> getApplicationsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getApplicationsByPostId(postId, pageable);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ApplicationRequest>> getApplicationsByUser(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        List<ApplicationRequest> applications = applicationRequestService.getApplicationsByUserId(userId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/post/{postId}/user")
    public ResponseEntity<ApplicationRequest> getApplicationByPostAndUser(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        return applicationRequestService.getApplicationByPostAndUser(postId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> countApplicationsByPostId(@PathVariable Long postId) {
        Long count = applicationRequestService.countApplicationsByPostId(postId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/received")
    public ResponseEntity<Page<ApplicationRequest>> getApplicationsForPosterUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationRequest> applications = applicationRequestService.getApplicationsForPosterUser(userId, pageable);
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<Void> approveApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        applicationRequestService.approveApplication(applicationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        applicationRequestService.rejectApplication(applicationId, userId);
        return ResponseEntity.noContent().build();
    }
} 