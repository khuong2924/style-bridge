package khuong.com.postingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime; // Thêm trường thời gian đặt lịch

@Entity
@Table(name = "dat_lich")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_dat_lich")
    private Long id;

    @Column(name = "ma_nguoi_dung_duoc_moi", nullable = false) // Người nghệ sĩ được chọn
    private Long invitedArtistUserId; // ID từ Account-Service

    @Column(name = "ma_nguoi_dung_dat_lich", nullable = false) // Người đăng bài tuyển, người tạo booking
    private Long bookingRequesterUserId; // ID từ Account-Service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_dang_tuyen", nullable = false)
    private RecruitmentPost recruitmentPost;
    
    // Thêm các trường cần thiết cho việc đặt lịch
    @Column(name = "thoi_gian_hen")
    private LocalDateTime bookingTime; // Thời gian cụ thể của lịch hẹn

    @Column(name = "dia_diem_hen", length = 255)
    private String bookingLocation; // Có thể khác với địa chỉ trong bài đăng tuyển

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "trang_thai_dat_lich", length = 20) // Ví dụ: PENDING_CONFIRMATION, CONFIRMED, CANCELED_BY_REQUESTER, CANCELED_BY_ARTIST, COMPLETED
    private String bookingStatus; 
}