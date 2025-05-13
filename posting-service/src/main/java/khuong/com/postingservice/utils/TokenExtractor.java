package khuong.com.postingservice.utils;

import khuong.com.postingservice.configs.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility class to extract user information from authentication tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenExtractor {

    private final JwtUtil jwtUtil;

    /**
     * Extracts the user ID from the authentication object
     * @param authentication The authentication object
     * @return The user ID as a Long
     * @throws IllegalStateException if authentication is null or principal cannot be converted to Long
     */
    public Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            log.error("Authentication object is null - user is not authenticated");
            throw new IllegalStateException("User not authenticated. Please provide a valid authentication token.");
        }
        
        try {
            String principalStr = authentication.getPrincipal().toString();
            log.debug("Principal from token: {}", principalStr);
            Long userId = Long.valueOf(principalStr);
            log.debug("User ID extracted from authentication: {}", userId);
            return userId;
        } catch (NumberFormatException | NullPointerException e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage());
            throw new IllegalStateException("Invalid token format. Unable to extract user ID.");
        }
    }
    
    /**
     * Extracts the user ID from the authorization header
     * @param authHeader The authorization header string (Bearer token)
     * @return The user ID as a String, or null if extraction failed
     */
    public String extractUserId(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        if (token != null) {
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    /**
     * Extracts the JWT token from the authorization header
     * @param authHeader The authorization header string
     * @return The JWT token, or null if the header is invalid
     */
    private String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
} 