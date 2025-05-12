package khuong.com.postingservice.configs.security;


import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor  // Use constructor injection
public class TokenExtractor {

    private final JwtUtil jwtUtil;

    public String extractUserId(String authHeader) {
        String token = extractTokenFromHeader(authHeader);
        if (token != null) {
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    private String extractTokenFromHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}             