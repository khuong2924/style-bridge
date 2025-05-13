package khuong.com.postingservice.service.impl;

import khuong.com.postingservice.configs.cloudinary.ImageUploadService;
import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.entity.AttachedImage;
import khuong.com.postingservice.entity.PersonalFeedPost;
import khuong.com.postingservice.enums.PrivacySetting;
import khuong.com.postingservice.repository.AttachedImageRepository;
import khuong.com.postingservice.repository.PersonalFeedPostRepository;
import khuong.com.postingservice.service.PersonalFeedPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalFeedPostServiceImpl implements PersonalFeedPostService {

    private final PersonalFeedPostRepository personalFeedPostRepository;
    private final AttachedImageRepository attachedImageRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public PersonalFeedPost createPost(PersonalFeedPost post, Long userId) {
        post.setPosterUserId(userId);
        post.setPostedAt(LocalDateTime.now());
        
        // Set default privacy if not specified
        if (post.getPrivacy() == null) {
            post.setPrivacy(PrivacySetting.PUBLIC);
        }
        
        return personalFeedPostRepository.save(post);
    }
    
    @Override
    @Transactional
    public PersonalFeedPost createPostWithImages(String content, String caption, String tags, 
                                               String privacy, List<MultipartFile> images, Long userId) throws IOException {
        // Create the post
        PersonalFeedPost post = new PersonalFeedPost();
        post.setContent(content);
        post.setCaption(caption);
        post.setTags(tags);
        
        // Set privacy
        if (privacy != null && !privacy.isEmpty()) {
            try {
                post.setPrivacy(PrivacySetting.valueOf(privacy.toUpperCase()));
            } catch (IllegalArgumentException e) {
                post.setPrivacy(PrivacySetting.PUBLIC);
            }
        } else {
            post.setPrivacy(PrivacySetting.PUBLIC);
        }
        
        // Save the post first to get an ID
        post = createPost(post, userId);
        
        // Upload and attach images if provided
        if (images != null && !images.isEmpty()) {
            addImagesToPost(post.getId(), images, userId);
        }
        
        return post;
    }
    
    @Override
    @Transactional
    public List<ImageInfo> addImagesToPost(Long postId, List<MultipartFile> images, Long userId) throws IOException {
        PersonalFeedPost post = personalFeedPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to add images to this post"));
        
        List<ImageInfo> uploadedImages = new ArrayList<>();
        int maxOrder = getMaxOrderForPost(postId);
        
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                
                AttachedImage attachedImage = AttachedImage.builder()
                        .storagePath(imageUrl)
                        .orderInAlbum(++maxOrder)
                        .personalFeedPost(post)
                        .build();
                
                attachedImage = attachedImageRepository.save(attachedImage);
                
                uploadedImages.add(new ImageInfo(
                        attachedImage.getId(),
                        attachedImage.getStoragePath(),
                        attachedImage.getOrderInAlbum()
                ));
            }
        }
        
        return uploadedImages;
    }
    
    @Override
    @Transactional
    public void deleteImageFromPost(Long postId, Long imageId, Long userId) {
        // Check if the post belongs to the user
        personalFeedPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete images from this post"));
        
        // Find the image
        AttachedImage image = attachedImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
        
        // Check if the image belongs to this post
        if (image.getPersonalFeedPost() == null || !image.getPersonalFeedPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Image does not belong to this post");
        }
        
        attachedImageRepository.delete(image);
    }
    
    @Override
    public List<ImageInfo> getPostImages(Long postId) {
        return attachedImageRepository.findByPersonalFeedPostIdOrderByOrderInAlbumAsc(postId)
                .stream()
                .map(img -> new ImageInfo(img.getId(), img.getStoragePath(), img.getOrderInAlbum()))
                .collect(Collectors.toList());
    }
    
    private int getMaxOrderForPost(Long postId) {
        return attachedImageRepository.findByPersonalFeedPostIdOrderByOrderInAlbumAsc(postId)
                .stream()
                .mapToInt(AttachedImage::getOrderInAlbum)
                .max()
                .orElse(0);
    }

    @Override
    @Transactional
    public PersonalFeedPost updatePost(Long postId, PersonalFeedPost updatedPost, Long userId) {
        PersonalFeedPost existingPost = personalFeedPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to update this post"));
        
        // Update fields
        existingPost.setContent(updatedPost.getContent());
        existingPost.setCaption(updatedPost.getCaption());
        existingPost.setTags(updatedPost.getTags());
        existingPost.setPrivacy(updatedPost.getPrivacy());
        
        return personalFeedPostRepository.save(existingPost);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        PersonalFeedPost post = personalFeedPostRepository.findByIdAndPosterUserId(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have permission to delete this post"));
        
        personalFeedPostRepository.delete(post);
    }

    @Override
    public Optional<PersonalFeedPost> getPostById(Long postId) {
        return personalFeedPostRepository.findById(postId);
    }

    @Override
    public Page<PersonalFeedPost> getAllPosts(Pageable pageable) {
        return personalFeedPostRepository.findAll(pageable);
    }

    @Override
    public Page<PersonalFeedPost> getPostsByUser(Long userId, Pageable pageable) {
        return personalFeedPostRepository.findByPosterUserId(userId, pageable);
    }
} 