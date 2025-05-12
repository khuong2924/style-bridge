package khuong.com.postingservice.controller;

import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import khuong.com.postingservice.service.RecruitmentPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class RecruitmentPostController {

    private final RecruitmentPostService recruitmentPostService;

    @PostMapping
    public ResponseEntity<RecruitmentPost> createPost(
            @Valid @RequestBody RecruitmentPost post,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getPrincipal().toString());
        RecruitmentPost createdPost = recruitmentPostService.createPost(post, userId);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
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
            @RequestParam(defaultValue = "postedAt") String sortBy,
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecruitmentPost> posts = recruitmentPostService.searchPosts(
                title, makeupType, status, startDate, endDate, pageable);
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