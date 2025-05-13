package khuong.com.postingservice.configs.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    public String extractUserId(String token) {
        try {
            // First try to get the id from the dedicated claim
            Claims claims = extractAllClaims(token);
            String userId = claims.get("id", String.class);
            log.debug("JwtService - Extracted userId from id claim: {}", userId);
            
            // If id claim is null, fall back to subject (username)
            if (userId == null) {
                userId = claims.getSubject();
                log.debug("JwtService - Extracted userId from subject (username): {}", userId);
            }
            
            return userId;
        } catch (Exception e) {
            log.error("JwtService - Error extracting userId: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            List<?> roles = claims.get("roles", List.class);
            if (roles == null) {
                log.debug("JwtService - No roles found in token");
                return new ArrayList<>();
            }
            log.debug("JwtService - Extracted roles: {}", roles);
            return roles.stream()
                    .map(Object::toString)
                    .toList();
        } catch (Exception e) {
            log.error("JwtService - Error extracting roles: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            log.debug("JwtService - Token is valid");
            return true;
        } catch (Exception e) {
            log.error("JwtService - Invalid token: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 