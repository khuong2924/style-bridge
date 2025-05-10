
package khuong.com.postingservice.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ApplicationRequestRequest {
    @NotNull(message = "Mã bài đăng tuyển không được để trống")
    private Long recruitmentPostId;
    private String message;
    private List<String> portfolioImageUrls; // URL ảnh portfolio đính kèm
}