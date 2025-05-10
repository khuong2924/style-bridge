package khuong.com.postingservice.payload.request;

import khuong.com.postingservice.enums.PrivacySetting;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PersonalFeedPostRequest {
    private String content;
    @NotNull(message = "Quyền riêng tư không được để trống")
    private PrivacySetting privacy;
    private List<String> imageUrls;
}