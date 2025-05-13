package khuong.com.authservice.controller;
import jakarta.validation.Valid;
import khuong.com.authservice.cloudinary.ImageUploadService;
import khuong.com.authservice.entity.User;
import khuong.com.authservice.entity.ERole;
import khuong.com.authservice.entity.Role;
import khuong.com.authservice.entity.UserRole;
import khuong.com.authservice.payload.request.*;
import khuong.com.authservice.payload.response.JwtResponse;
import khuong.com.authservice.payload.response.MessageResponse;
import khuong.com.authservice.repository.UserRepository;
import khuong.com.authservice.repository.RoleRepository;
import khuong.com.authservice.security.JwtUtils;
import khuong.com.authservice.security.UserDetailsImpl;
import khuong.com.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    ImageUploadService imageUploadService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @Valid @RequestPart("username") String username,
            @Valid @RequestPart("email") String email,
            @Valid @RequestPart("password") String password,
            @RequestPart(value = "fullName", required = false) String fullName,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "gender", required = false) String gender,
            @RequestPart(value = "roles", required = false) Set<String> roles,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        
        // Check if username exists
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Check if email exists
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user
        User user = new User(username, email, encoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);
        user.setGender(gender);
        
        // Upload avatar if provided
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarUrl = imageUploadService.uploadImage(avatar);
                user.setAvatarUrl(avatarUrl);
            } catch (IOException e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Failed to upload avatar."));
            }
        }

        // Set user roles
        Set<UserRole> userRoles = new HashSet<>();

        if (roles == null || roles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            userRoles.add(new UserRole(user, userRole));
        } else {
            roles.forEach(role -> {
                ERole eRole;
                try {
                    eRole = ERole.valueOf(role);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Error: Role " + role + " is not found.");
                }
                Role foundRole = roleRepository.findByName(eRole)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
                userRoles.add(new UserRole(user, foundRole));
            });
        }

        user.setRoles(userRoles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return authService.logout();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}