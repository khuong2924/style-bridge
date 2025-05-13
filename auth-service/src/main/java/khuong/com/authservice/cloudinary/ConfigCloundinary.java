package khuong.com.authservice.cloudinary;


import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConfigCloundinary {
    
    @Value("${cloudinary.cloud-name:decz34g1a}")
    private String cloudName;
    
    @Value("${cloudinary.api-key:325126569821533}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret:wqcbw8JTOaND-yfguO_6p8NhwHc}")
    private String apiSecret;

    @Bean
    public Cloudinary configKey() {
        try {
            Map<String, String> config = new HashMap<>();
            
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            return new Cloudinary(config);
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary configuration error", e);
        }
    }
}
