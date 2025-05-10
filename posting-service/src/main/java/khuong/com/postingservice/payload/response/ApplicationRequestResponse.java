
package khuong.com.postingservice.payload.response;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.dto.UserInfoShortResponse;
import khuong.com.postingservice.enums.ApplicationRequestStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApplicationRequestResponse {
    private Long id;
    private LocalDateTime submittedAt;
    private String message;
    private ApplicationRequestStatus status;
    private Long recruitmentPostId;
    private Long applicantUserId;
    private UserInfoShortResponse applicantInfo;
    private List<ImageInfo> attachedImages;
}
