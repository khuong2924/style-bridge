package khuong.com.postingservice.payload.response;

import lombok.Data;
import java.time.LocalDateTime;

import khuong.com.postingservice.dto.RecruitmentPostSummaryResponse;
import khuong.com.postingservice.dto.UserInfoShortResponse;

@Data
public class BookingResponse {
    private Long id;
    private Long invitedArtistUserId;
    private UserInfoShortResponse invitedArtistInfo;
    private Long bookingRequesterUserId;
    private UserInfoShortResponse bookingRequesterInfo;
    private Long recruitmentPostId;
    private RecruitmentPostSummaryResponse recruitmentPostSummary; 
    private LocalDateTime bookingTime;
    private String bookingLocation;
    private String notes;
    private String bookingStatus;
}