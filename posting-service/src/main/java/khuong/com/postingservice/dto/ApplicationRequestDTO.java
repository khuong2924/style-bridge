package khuong.com.postingservice.dto;

import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.entity.RecruitmentPost;
import khuong.com.postingservice.enums.ApplicationStatus;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ApplicationRequestDTO {
    private Long id;
    private String message;
    private String contactInfo;
    private ApplicationStatus status;
    private RecruitmentPostDTO recruitmentPost; // Sử dụng DTO cho RecruitmentPost
    private Long applicantUserId;
    private List<ImageInfo> attachedImages;

    public static ApplicationRequestDTO fromEntity(ApplicationRequest entity) {
        ApplicationRequestDTO dto = new ApplicationRequestDTO();
        dto.setId(entity.getId());
        dto.setMessage(entity.getMessage());
        dto.setContactInfo(entity.getContactInfo());
        dto.setStatus(entity.getStatus());
        dto.setApplicantUserId(entity.getApplicantUserId());

        // Xử lý và chuyển đổi RecruitmentPost một cách an toàn
        if (entity.getRecruitmentPost() != null) {
            RecruitmentPostDTO postDTO = new RecruitmentPostDTO();
            RecruitmentPost post = entity.getRecruitmentPost();
            postDTO.setId(post.getId());
            postDTO.setTitle(post.getTitle());
            postDTO.setMakeupType(post.getMakeupType());
            postDTO.setStartTime(post.getStartTime());
            postDTO.setExpectedDuration(post.getExpectedDuration());
            postDTO.setAddress(post.getAddress());
            postDTO.setHiringType(post.getHiringType());
            postDTO.setCompensation(post.getCompensation());
            postDTO.setQuantity(post.getQuantity());
            postDTO.setDescription(post.getDescription());
            postDTO.setDeadline(post.getDeadline());
            postDTO.setPostedAt(post.getPostedAt());
            postDTO.setStatus(post.getStatus());
            postDTO.setPosterUserId(post.getPosterUserId());

            dto.setRecruitmentPost(postDTO);
        }

        // Xử lý hình ảnh
        if (entity.getAttachedImages() != null) {
            dto.setAttachedImages(entity.getAttachedImages().stream()
                .map(img -> new ImageInfo(img.getId(), img.getStoragePath(), img.getOrderInAlbum()))
                .collect(Collectors.toList()));
        }

        return dto;
    }
}