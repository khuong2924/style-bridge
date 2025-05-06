package khuong.com.authservice.service;

import khuong.com.authservice.entity.ERole;
import khuong.com.authservice.entity.Role;
import khuong.com.authservice.entity.User;
import khuong.com.authservice.entity.UserRole;
import khuong.com.authservice.payload.request.*;
import khuong.com.authservice.payload.response.JwtResponse;
import khuong.com.authservice.payload.response.MessageResponse;
import khuong.com.authservice.repository.RoleRepository;
import khuong.com.authservice.repository.UserRepository;
import khuong.com.authservice.security.JwtUtils;
import khuong.com.authservice.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder encoder;

    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<UserRole> userRoles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_WAITER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            userRoles.add(new UserRole(user, userRole));
        } else {
            strRoles.forEach(role -> {
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


    public ResponseEntity<?> login(LoginRequest loginRequest) {
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
                roles));  // Thêm roles vào response
    }

    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }

    public ResponseEntity<?> refreshToken(TokenRefreshRequest request) {
        String token = request.getRefreshToken();
        if (jwtUtils.validateJwtToken(token)) {
            String newToken = jwtUtils.generateJwtTokenFromRefreshToken(token);
            return ResponseEntity.ok(new MessageResponse("Token refreshed: " + newToken));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid refresh token"));
        }
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.ok(new MessageResponse("Password reset link sent"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Email not found"));
        }
    }

    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        if (jwtUtils.validateJwtToken(token)) {
            String newPassword = request.getNewPassword();
            User user = userRepository.findByEmail(jwtUtils.getUserNameFromJwtToken(token)).orElse(null);
            if (user != null) {
                user.setPassword(encoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok(new MessageResponse("Password reset successful"));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid reset token"));
        }
    }
}