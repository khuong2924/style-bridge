package khuong.com.postingservice.configs.security;


import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("securityTokenExtractor")
@RequiredArgsConstructor
@Slf4j
public class SecurityTokenExtractor {

    private final JwtUtil jwtUtil;

    public String extractUserId(String authHeader) {
        log.debug("Extracting user ID from Authorization header");
        if (authHeader == null || authHeader.isEmpty()) {
            log.debug("Authorization header is null or empty");
            return null;
        }
        
        String token = extractTokenFromHeader(authHeader);
        if (token != null) {
            String userId = jwtUtil.extractUserId(token);
            log.debug("Extracted user ID: {}", userId);
            return userId;
        }
        log.debug("Failed to extract token from header");
        return null;
    }

    private String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Extracted token from header: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
            return token;
        }
        return null;
    }
}             