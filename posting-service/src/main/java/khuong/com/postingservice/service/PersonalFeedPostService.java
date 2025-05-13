package khuong.com.postingservice.service;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.PersonalFeedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface PersonalFeedPostService {
    
    PersonalFeedPost createPost(PersonalFeedPost post, Long userId);
    
    PersonalFeedPost createPostWithImages(String content, String caption, String tags, 
                                         String privacy, List<MultipartFile> images, Long userId) throws IOException;
    
    PersonalFeedPost updatePost(Long postId, PersonalFeedPost updatedPost, Long userId);
    
    void deletePost(Long postId, Long userId);
    
    Optional<PersonalFeedPost> getPostById(Long postId);
    
    Page<PersonalFeedPost> getAllPosts(Pageable pageable);
    
    Page<PersonalFeedPost> getPostsByUser(Long userId, Pageable pageable);
    
    List<ImageInfo> addImagesToPost(Long postId, List<MultipartFile> images, Long userId) throws IOException;
    
    void deleteImageFromPost(Long postId, Long imageId, Long userId);
    
    List<ImageInfo> getPostImages(Long postId);
} 