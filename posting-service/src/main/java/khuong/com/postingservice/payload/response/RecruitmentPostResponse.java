package khuong.com.postingservice.payload.response;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.dto.UserInfoShortResponse;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecruitmentPostResponse {
    private Long id;
    private LocalDateTime postedAt;
    private String title;
    private String makeupType;
    private LocalDateTime startTime;
    private String expectedDuration;
    private String address;
    private String hiringType;
    private String compensation;
    private Integer quantity;
    private String description;
    private LocalDateTime deadline;
    private RecruitmentPostStatus status;
    private Long posterUserId; // Để client có thể fetch thông tin người đăng
    private UserInfoShortResponse posterInfo; // Thông tin rút gọn người đăng (sẽ gọi Account-Service để lấy)
    private List<ImageInfo> attachedImages;
    private Integer totalApplications; // Tổng số đơn ứng tuyển
}