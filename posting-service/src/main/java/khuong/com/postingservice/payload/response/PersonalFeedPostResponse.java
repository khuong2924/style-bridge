package khuong.com.postingservice.payload.response;

import khuong.com.postingservice.dto.ImageInfo;
import khuong.com.postingservice.dto.UserInfoShortResponse;
import khuong.com.postingservice.enums.PrivacySetting;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PersonalFeedPostResponse {
    private Long id;
    private LocalDateTime postedAt;
    private String content;
    private PrivacySetting privacy;
    private Long posterUserId;
    private UserInfoShortResponse posterInfo;
    private List<ImageInfo> attachedImages;
}
