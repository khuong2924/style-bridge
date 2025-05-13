package khuong.com.postingservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import khuong.com.postingservice.enums.RecruitmentPostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bai_dang_tuyen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_dang")
    private Long id;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String title;

    @Column(name = "loai_trang_diem", length = 100)
    private String makeupType;

    @Column(name = "thoi_gian_bat_dau")
    private LocalDateTime startTime;

    @Column(name = "thoi_luong_du_kien", length = 50)
    private String expectedDuration; // Ví dụ: "2 tiếng", "1 buổi"

    @Column(name = "dia_chi", nullable = false, length = 255)
    private String address;

    @Column(name = "hinh_thuc_thue", length = 50)
    private String hiringType; // Ví dụ: "trọn gói", "theo giờ"

    @Column(name = "thu_lao", length = 100) // Có thể dùng BigDecimal nếu cần tính toán chính xác
    private String compensation;

    @Column(name = "so_luong_tuyen")
    private Integer quantity;

    @Lob // For large text objects
    @Column(name = "mo_ta_them", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thoi_han")
    private LocalDateTime deadline;

    @Column(name = "ngay_dang")
    private LocalDateTime postedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private RecruitmentPostStatus status;

    @Column(name = "ma_nguoi_dung_dang_tai", nullable = false)
    private Long posterUserId; // ID từ Account-Service

    @OneToMany(mappedBy = "recruitmentPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationRequest> applicationRequests;

    @OneToMany(mappedBy = "recruitmentPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("post-images")
    private List<AttachedImage> attachedImages;

    @OneToMany(mappedBy = "recruitmentPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;
}
