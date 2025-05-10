package khuong.com.postingservice.payload.request;

import khuong.com.postingservice.enums.ApplicationRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequestStatusUpdateRequest {
    @NotNull
    private ApplicationRequestStatus status;
}