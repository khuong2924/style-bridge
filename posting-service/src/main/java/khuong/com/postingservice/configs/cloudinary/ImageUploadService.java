package khuong.com.postingservice.configs.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    @Autowired
    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            if (file == null || file.isEmpty() || file.getBytes().length == 0) {
                log.error("Invalid file: file is null or empty");
                throw new IllegalArgumentException("Invalid file: file is null or empty");
            }
            
            log.info("Uploading file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "auto");
            options.put("folder", "posting-service");
            
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            
            String url = (String) uploadResult.get("url");
            log.info("File uploaded successfully: {}", url);
            
            return url;
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public String getImageUrl(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            return null;
        }
        return cloudinary.url().resourceType("image").publicId(publicId).generate();
    }
}