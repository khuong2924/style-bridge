package khuong.com.postingservice.controller;

import khuong.com.postingservice.configs.cloudinary.ImageUploadService;
import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.AttachedImage;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import khuong.com.postingservice.service.RecruitmentPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.text.SimpleDateFormat;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class RecruitmentPostController {

    private final RecruitmentPostService recruitmentPostService;
    private final ImageUploadService imageUploadService;

    @PostMapping
    public ResponseEntity<RecruitmentPost> createPost(
            @Valid @RequestBody RecruitmentPost post,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        RecruitmentPost createdPost = recruitmentPostService.createPost(post, userId);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }
    
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithImages(
            @RequestParam("title") String title,
            @RequestParam("makeupType") String makeupType,
            @RequestParam("expectedDuration") String expectedDuration,
            @RequestParam("address") String address,
            @RequestParam(value = "hiringType", required = false) String hiringType,
            @RequestParam(value = "compensation", required = false) String compensation,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "startTime", required = false) String startTimeStr,
            @RequestParam(value = "deadline", required = false) String deadlineStr,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication) {
        
        try {
            log.info("Creating post with title: {}", title);
            log.info("Auth header present: {}", authHeader != null ? "Yes" : "No");
            
            // Kiểm tra xác thực chi tiết hơn
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Authorization header missing or invalid format");
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header. Please provide a valid Bearer token.");
            }
            
            if (authentication == null) {
                log.error("Authentication object is null - user is not authenticated");
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated. Please provide a valid authentication token.");
            }
            
            String principal = authentication.getPrincipal().toString();
            log.info("Authentication principal: {}", principal);
            
            // Extract numeric ID from principal (which might be in format "user1")
            Long userId;
            try {
                if (principal.startsWith("user")) {
                    // Extract the numeric part from strings like "user1"
                    String idPart = principal.substring(4); // Skip "user" prefix
                    userId = Long.valueOf(idPart);
                    log.info("Extracted user ID {} from username {}", userId, principal);
                } else {
                    // Try direct conversion if it doesn't match the pattern
                    userId = Long.valueOf(principal);
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                log.error("Failed to extract user ID from principal: {}", principal, e);
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Could not extract user ID from authentication principal: " + principal);
            }
            
            log.info("User ID extracted from authentication: {}", userId);
            
            // Create RecruitmentPost object
            RecruitmentPost post = new RecruitmentPost();
            post.setTitle(title);
            post.setMakeupType(makeupType);
            post.setExpectedDuration(expectedDuration);
            post.setAddress(address);
            post.setHiringType(hiringType);
            post.setCompensation(compensation);
            post.setQuantity(quantity);
            post.setDescription(description);
            
            // Set LocalDateTime fields
            LocalDateTime now = LocalDateTime.now();
            post.setPostedAt(now);
            
            // Define formatters for different possible date formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // ISO format: 2023-05-14T10:15:30
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
            };
            
            // Parse startTime if provided
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                boolean parsed = false;
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
                        post.setStartTime(startTime);
                        parsed = true;
                        break;
                    } catch (DateTimeParseException e) {
                        // Try next formatter
                    }
                }
                
                if (!parsed) {
                    log.error("Error parsing startTime: {}", startTimeStr);
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Invalid startTime format. Please use ISO-8601 format (yyyy-MM-ddTHH:mm:ss) or yyyy-MM-dd HH:mm:ss");
                }
            }
            
            // Parse deadline if provided
            if (deadlineStr != null && !deadlineStr.isEmpty()) {
                boolean parsed = false;
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        LocalDateTime deadline = LocalDateTime.parse(deadlineStr, formatter);
                        post.setDeadline(deadline);
                        parsed = true;
                        break;
                    } catch (DateTimeParseException e) {
                        // Try next formatter
                    }
                }
                
                if (!parsed) {
                    log.error("Error parsing deadline: {}", deadlineStr);
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Invalid deadline format. Please use ISO-8601 format (yyyy-MM-ddTHH:mm:ss) or yyyy-MM-dd HH:mm:ss");
                }
            }
            
            // Save post
            RecruitmentPost createdPost = recruitmentPostService.createPost(post, userId);
            log.info("Post created with ID: {}", createdPost.getId());
            
            // Upload images if provided
            if (images != null && !images.isEmpty()) {
                log.info("Uploading {} images", images.size());
                try {
                    List<ImageInfo> uploadedImages = recruitmentPostService.addImagesToPost(createdPost.getId(), images, userId);
                    log.info("Successfully uploaded {} images", uploadedImages.size());
                } catch (IOException e) {
                    log.error("Error uploading images: {}", e.getMessage());
                    // Continue with post creation even if image upload fails
                }
            } else {
                log.info("No images provided");
            }
            
            // Fetch the post with attached images
            RecruitmentPost postWithImages = recruitmentPostService.getPostById(createdPost.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve the created post"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", postWithImages.getId());
            response.put("title", postWithImages.getTitle());
            response.put("status", postWithImages.getStatus());
            response.put("address", postWithImages.getAddress());
            response.put("message", "Post created successfully");
            
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating post with images: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageInfo>> uploadImages(
            @PathVariable Long postId,
            @RequestParam("images") List<MultipartFile> images,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        
        try {
            List<ImageInfo> uploadedImages = recruitmentPostService.addImagesToPost(postId, images, userId);
            return ResponseEntity.ok(uploadedImages);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{postId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long postId,
            @PathVariable Long imageId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        recruitmentPostService.deleteImageFromPost(postId, imageId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{postId}/images")
    public ResponseEntity<List<ImageInfo>> getPostImages(
            @PathVariable Long postId) {
        List<ImageInfo> images = recruitmentPostService.getPostImages(postId);
        return ResponseEntity.ok(images);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<RecruitmentPost> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody RecruitmentPost post,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        RecruitmentPost updatedPost = recruitmentPostService.updatePost(postId, post, userId);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        recruitmentPostService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<RecruitmentPost> getPostById(@PathVariable Long postId) {
        return recruitmentPostService.getPostById(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<RecruitmentPost>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<RecruitmentPost> posts = recruitmentPostService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<RecruitmentPost>> getPostsByStatus(
            @PathVariable RecruitmentPostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecruitmentPost> posts = recruitmentPostService.getPostsByStatus(status, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<RecruitmentPost>> getPostsByUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<RecruitmentPost> posts = recruitmentPostService.getPostsByUser(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RecruitmentPost>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String makeupType,
            @RequestParam(required = false) RecruitmentPostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecruitmentPost> posts = recruitmentPostService.searchPosts(title, makeupType, status, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecruitmentPost>> getRecentPosts(
            @RequestParam(defaultValue = "ACTIVE") RecruitmentPostStatus status,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecruitmentPost> posts = recruitmentPostService.getRecentPosts(status, limit);
        return ResponseEntity.ok(posts);
    }

    @PatchMapping("/{postId}/status")
    public ResponseEntity<Void> updatePostStatus(
            @PathVariable Long postId,
            @RequestParam RecruitmentPostStatus status,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        recruitmentPostService.updatePostStatus(postId, status, userId);
        return ResponseEntity.noContent().build();
    }
}