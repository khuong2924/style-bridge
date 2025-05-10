// com/example/postingservice/dto/BookingRequest.java
package khuong.com.postingservice.payload.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull(message = "Mã người dùng được mời không được để trống")
    private Long invitedArtistUserId;

    @NotNull(message = "Mã bài đăng tuyển không được để trống")
    private Long recruitmentPostId;

    @NotNull(message = "Thời gian hẹn không được để trống")
    @FutureOrPresent(message = "Thời gian hẹn phải là hiện tại hoặc tương lai")
    private LocalDateTime bookingTime;
    
    private String bookingLocation;
    private String notes;
  
}
