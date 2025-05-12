package khuong.com.postingservice.entity;

import khuong.com.postingservice.enums.BookingStatus;
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

    @Column(name = "ma_nguoi_dung_khach_hang", nullable = false)
    private Long clientUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_dang_tuyen", nullable = false)
    private RecruitmentPost recruitmentPost;
    
    @Column(name = "thoi_gian_hen")
    private LocalDateTime bookingTime;

    @Column(name = "dia_diem_hen", length = 255)
    private String location;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_dat_lich", length = 20)
    private BookingStatus status;
    
    @Column(name = "thoi_gian_tao")
    private LocalDateTime createdAt;
    
    @Column(name = "thoi_gian_xac_nhan")
    private LocalDateTime confirmedAt;
    
    @Column(name = "thoi_gian_huy")
    private LocalDateTime cancelledAt;
    
    @Column(name = "nguoi_dung_huy")
    private Long cancelledBy;
    
    @Column(name = "ly_do_huy", length = 255)
    private String cancelReason;
    
    @Column(name = "thoi_gian_hoan_thanh")
    private LocalDateTime completedAt;
}