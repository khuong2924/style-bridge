package khuong.com.postingservice.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class RequestLoggingFilterConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                
                // Only log for /api/posts/with-images endpoint
                if (request.getRequestURI().contains("/api/posts/with-images")) {
                    log.info("=== DEBUG: Request Headers for {} {} ===", request.getMethod(), request.getRequestURI());
                    
                    Collections.list(request.getHeaderNames()).forEach(headerName -> {
                        log.info("Header '{}': {}", headerName, request.getHeader(headerName));
                    });
                    
                    log.info("=== End of Headers ===");
                    
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        log.info("Authorization header is present and starts with 'Bearer '");
                    } else {
                        log.warn("Authorization header is missing or invalid: {}", authHeader);
                    }
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
} 