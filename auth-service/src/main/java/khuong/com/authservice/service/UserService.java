package khuong.com.authservice.service;



import khuong.com.authservice.cloudinary.ImageUploadService;
import khuong.com.authservice.dto.UpdateUserDTO;
import khuong.com.authservice.dto.UserDTO;
import khuong.com.authservice.entity.User;
import khuong.com.authservice.exception.ResourceNotFoundException;
import khuong.com.authservice.repository.UserRepository;
import khuong.com.authservice.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ImageUploadService imageUploadService;

    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserDTO(user);
    }

    public UserDTO updateUserProfile(Long userId, UpdateUserDTO updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getAddress() != null) {
            user.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getGender() != null) {
            user.setGender(updateRequest.getGender());
        }

        user = userRepository.save(user);
        return mapToUserDTO(user);
    }
    
    public UserDTO updateUserAvatar(Long userId, MultipartFile avatar) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar);
            user.setAvatarUrl(avatarUrl);
            user = userRepository.save(user);
        }
        
        return mapToUserDTO(user);
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setPhone(user.getPhone());
        userDTO.setAddress(user.getAddress());
        userDTO.setGender(user.getGender());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        return userDTO;
    }





    public UserDTO getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new ResourceNotFoundException("Not authenticated");
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof String) {
                try {
                    // The principal is a userId since we're using JWT
                    Long userId = Long.parseLong((String) principal);
                    return getUserProfile(userId);
                } catch (NumberFormatException e) {
                    // If it's not a number, it might be a username
                    User user = userRepository.findByUsername((String) principal)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return mapToUserDTO(user);
                }
            } else if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                return getUserProfile(userDetails.getId());
            } else if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                return mapToUserDTO(user);
            } else {
                throw new ResourceNotFoundException("Unknown principal type: " + (principal != null ? principal.getClass().getName() : "null"));
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error getting current user: " + e.getMessage());
        }
    }
}