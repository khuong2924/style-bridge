package khuong.com.postingservice.payload.request;

import khuong.com.postingservice.enums.RecruitmentPostStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecruitmentPostRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String makeupType;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @FutureOrPresent(message = "Thời gian bắt đầu phải ở hiện tại hoặc tương lai")
    private LocalDateTime startTime;

    private String expectedDuration;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String hiringType;
    private String compensation;

    @Min(value = 1, message = "Số lượng tuyển phải ít nhất là 1")
    private Integer quantity;

    private String description;

    @NotNull(message = "Thời hạn bài đăng không được để trống")
    @Future(message = "Thời hạn bài đăng phải ở tương lai")
    private LocalDateTime deadline;
    
    // Chỉ dùng khi cập nhật, không dùng khi tạo mới (sẽ set mặc định là RECRUITING)
    private RecruitmentPostStatus status; 
    
    // List of image URLs or IDs if already uploaded
    private List<String> imageUrls; 
}