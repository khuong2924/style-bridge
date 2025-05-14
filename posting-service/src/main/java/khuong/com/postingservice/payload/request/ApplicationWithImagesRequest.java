package khuong.com.postingservice.payload.request;

import lombok.Data;

@Data
public class ApplicationWithImagesRequest {
    private Long recruitmentPostId;
    private String message;
    private String otherSkills;
    private String preferredContactMethod;
    private String availability;
}