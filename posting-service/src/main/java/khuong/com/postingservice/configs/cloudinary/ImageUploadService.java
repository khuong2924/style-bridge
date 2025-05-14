package khuong.com.postingservice.configs.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * Uploads an image to Cloudinary cloud storage.
     * This method uses REQUIRES_NEW propagation to ensure it runs in its own transaction
     * since Cloudinary operations should be independent of database transactions.
     *
     * @param file The image file to upload
     * @return The URL of the uploaded image
     * @throws IOException If file reading or upload fails
     * @throws IllegalArgumentException If the file is invalid
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("Invalid file: file is null or empty");
            throw new IllegalArgumentException("Invalid file: file is null or empty");
        }
        
        // Step 1: Read file bytes (separate this from uploading for better error handling)
        byte[] fileBytes;
        try {
            log.debug("Reading file bytes from: {}", file.getOriginalFilename());
            fileBytes = file.getBytes();
            if (fileBytes.length == 0) {
                log.error("Invalid file: file has zero bytes");
                throw new IllegalArgumentException("Invalid file: file has zero bytes");
            }
            log.debug("Successfully read {} bytes from file", fileBytes.length);
        } catch (IOException e) {
            log.error("Cannot read file bytes: {}", e.getMessage(), e);
            throw new IOException("Cannot read file bytes: " + e.getMessage(), e);
        }
        
        log.info("Uploading file: {}, size: {} bytes, content type: {}", 
                file.getOriginalFilename(), fileBytes.length, file.getContentType());
        
        // Step 2: Upload to Cloudinary
        try {
            // Configure upload options
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "auto");
            options.put("folder", "posting-service");
            
            // Upload to Cloudinary (which is an external service, outside of our DB transaction)
            log.debug("Sending upload request to Cloudinary");
            Map<String, Object> uploadResult = cloudinary.uploader().upload(fileBytes, options);
            
            String url = (String) uploadResult.get("url");
            String publicId = (String) uploadResult.get("public_id");
            log.info("File uploaded successfully to Cloudinary. URL: {}, Public ID: {}", url, publicId);
            
            return url;
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Error uploading file to Cloudinary: " + e.getMessage(), e);
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