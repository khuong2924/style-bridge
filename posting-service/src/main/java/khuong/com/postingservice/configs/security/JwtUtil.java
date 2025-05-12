package khuong.com.postingservice.configs.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
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
        Claims claims = extractAllClaims(token);
        return claims.get("id", String.class);
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}