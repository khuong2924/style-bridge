package khuong.com.postingservice.configs.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    // Add a default constructor to handle null injection
    public JwtUtil() {
        // Default constructor
    }
    
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public String extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // First try to get the id from the dedicated claim
            String userId = claims.get("id", String.class);
            // If the "id" claim is not present, fall back to the subject claim
            if (userId == null) {
                userId = claims.getSubject();
                log.debug("UserId from subject (username): {}", userId);
            } else {
                log.debug("UserId from id claim: {}", userId);
            }
            return userId;
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}