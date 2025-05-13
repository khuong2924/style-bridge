package khuong.com.postingservice.controller;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.PersonalFeedPost;
import khuong.com.postingservice.service.PersonalFeedPostService;
import khuong.com.postingservice.utils.TokenExtractor;
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

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Slf4j
public class PersonalFeedPostController {

    private final PersonalFeedPostService personalFeedPostService;
    private final TokenExtractor tokenExtractor;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PersonalFeedPost> createPost(
            @RequestPart("content") String content,
            @RequestPart(value = "caption", required = false) String caption,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "privacy", required = false) String privacy,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        
        try {
            Long userId = tokenExtractor.extractUserId(authentication);
            
            PersonalFeedPost createdPost = personalFeedPostService.createPostWithImages(
                    content, caption, tags, privacy, images, userId);
            return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IOException e) {
            log.error("Error creating post with images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageInfo>> uploadImages(
            @PathVariable Long postId,
            @RequestParam("images") List<MultipartFile> images,
            Authentication authentication) {
        
        try {
            Long userId = tokenExtractor.extractUserId(authentication);
            
            List<ImageInfo> uploadedImages = personalFeedPostService.addImagesToPost(postId, images, userId);
            return ResponseEntity.ok(uploadedImages);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IOException e) {
            log.error("Error uploading images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{postId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long postId,
            @PathVariable Long imageId,
            Authentication authentication) {
        
        try {
            Long userId = tokenExtractor.extractUserId(authentication);
            personalFeedPostService.deleteImageFromPost(postId, imageId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @GetMapping("/{postId}/images")
    public ResponseEntity<List<ImageInfo>> getPostImages(
            @PathVariable Long postId) {
        
        List<ImageInfo> images = personalFeedPostService.getPostImages(postId);
        return ResponseEntity.ok(images);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PersonalFeedPost> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PersonalFeedPost post,
            Authentication authentication) {
        
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        PersonalFeedPost updatedPost = personalFeedPostService.updatePost(postId, post, userId);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        personalFeedPostService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PersonalFeedPost> getPostById(
            @PathVariable Long postId) {
        
        return personalFeedPostService.getPostById(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<PersonalFeedPost>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<PersonalFeedPost> posts = personalFeedPostService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<PersonalFeedPost>> getPostsByUser(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        Pageable pageable = PageRequest.of(page, size);
        Page<PersonalFeedPost> posts = personalFeedPostService.getPostsByUser(userId, pageable);
        return ResponseEntity.ok(posts);
    }
} 