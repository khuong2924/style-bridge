package khuong.com.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import khuong.com.authservice.dto.UpdateUserDTO;
import khuong.com.authservice.dto.UserDTO;
import khuong.com.authservice.exception.ResourceNotFoundException;
import khuong.com.authservice.payload.response.MessageResponse;
import khuong.com.authservice.repository.UserRepository;
import khuong.com.authservice.security.UserDetailsImpl;
import khuong.com.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        UserDTO userResponse = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody UpdateUserDTO updateRequest) {
        UserDTO userResponse = userService.updateUserProfile(currentUser.getId(), updateRequest);
        return ResponseEntity.ok(userResponse);
    }
    
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserAvatar(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam("avatar") MultipartFile avatar) {
        try {
            UserDTO userResponse = userService.updateUserAvatar(currentUser.getId(), avatar);
            return ResponseEntity.ok(userResponse);
        } catch (IOException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Failed to upload avatar."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            UserDTO userResponse = userService.getCurrentUser();
            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(401)
                .body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserDTO userResponse = userService.getUserProfile(userId);
            return ResponseEntity.ok(userResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(new MessageResponse("Error: User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request,response, auth);
        }
        return ResponseEntity.ok("Logout successfully");

    }

}