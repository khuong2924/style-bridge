package khuong.com.postingservice.dto;

import khuong.com.postingservice.entity.ApplicationRequest;
import khuong.com.postingservice.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestListDTO {
    private Long id;
    private Long recruitmentPostId;
    private String recruitmentPostTitle;
    private Long applicantUserId;
    private ApplicationStatus status;
    
    public static ApplicationRequestListDTO fromEntity(ApplicationRequest entity) {
        return ApplicationRequestListDTO.builder()
            .id(entity.getId())
            .recruitmentPostId(entity.getRecruitmentPost() != null ? entity.getRecruitmentPost().getId() : null)
            .recruitmentPostTitle(entity.getRecruitmentPost() != null ? entity.getRecruitmentPost().getTitle() : null)
            .applicantUserId(entity.getApplicantUserId())
            .status(entity.getStatus())
            .build();
    }
}