package khuong.com.postingservice.dto;

import khuong.com.postingservice.entity.Booking;
import khuong.com.postingservice.enums.BookingStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookingDTO {
    private Long id;
    private Long clientUserId;
    private Long recruitmentPostId;
    private String recruitmentPostTitle;
    private LocalDateTime bookingDate;
    private String location;
    private String notes;
    private BookingStatus status;
    private Long cancelledBy;
    private String cancelReason;

    public static BookingDTO fromEntity(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setClientUserId(booking.getClientUserId());
        
        if (booking.getRecruitmentPost() != null) {
            dto.setRecruitmentPostId(booking.getRecruitmentPost().getId());
            dto.setRecruitmentPostTitle(booking.getRecruitmentPost().getTitle());
        }
        
        dto.setBookingDate(booking.getBookingDate());
        dto.setLocation(booking.getLocation());
        dto.setNotes(booking.getNotes());
        dto.setStatus(booking.getStatus());
        dto.setCancelledBy(booking.getCancelledBy());
        dto.setCancelReason(booking.getCancelReason());
        
        return dto;
    }
} 