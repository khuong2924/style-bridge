package khuong.com.postingservice.dto;

import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import lombok.Data;

import java.time.LocalDateTime;
import org.hibernate.Hibernate;

@Data
public class RecruitmentPostDTO {
    private Long id;
    private String title;
    private String makeupType;
    private LocalDateTime startTime;
    private String expectedDuration;
    private String address;
    private String hiringType;
    private String compensation;
    private Integer quantity;
    private String description;
    private LocalDateTime postedAt;
    private LocalDateTime deadline;
    private RecruitmentPostStatus status;
    private Long posterUserId;
    private int imageCount;
    
    public static RecruitmentPostDTO fromEntity(RecruitmentPost entity) {
        RecruitmentPostDTO dto = new RecruitmentPostDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMakeupType(entity.getMakeupType());
        dto.setStartTime(entity.getStartTime());
        dto.setExpectedDuration(entity.getExpectedDuration());
        dto.setAddress(entity.getAddress());
        dto.setHiringType(entity.getHiringType());
        dto.setCompensation(entity.getCompensation());
        dto.setQuantity(entity.getQuantity());
        dto.setDescription(entity.getDescription());
        dto.setPostedAt(entity.getPostedAt());
        dto.setDeadline(entity.getDeadline());
        dto.setStatus(entity.getStatus());
        dto.setPosterUserId(entity.getPosterUserId());
        
        // Safely handle image count
        if (entity.getAttachedImages() != null && Hibernate.isInitialized(entity.getAttachedImages())) {
            dto.setImageCount(entity.getAttachedImages().size());
        } else {
            // If images are not initialized, we leave it as 0
            dto.setImageCount(0);
        }
        
        return dto;
    }
}