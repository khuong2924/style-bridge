package khuong.com.authservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.security.Key;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Bean
    public Key key() {
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(jwtSecret));
    }

    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }
}