
package khuong.com.postingservice.entity;

import khuong.com.postingservice.enums.ApplicationRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "yeu_cau_ung_tuyen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_yeu_cau")
    private Long id;

    @CreationTimestamp
    @Column(name = "thoi_diem_gui", updatable = false)
    private LocalDateTime submittedAt;

    @Lob
    @Column(name = "loi_nhan", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private ApplicationRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_dang_tuyen", nullable = false)
    private RecuitmentPost recruitmentPost;

    @Column(name = "ma_nguoi_dung_ung_tuyen", nullable = false) // Thêm trường này để biết ai ứng tuyển
    private Long applicantUserId; // ID từ Account-Service

    @OneToMany(mappedBy = "applicationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedImage> attachedImages; // Ảnh portfolio của người ứng tuyển cho yêu cầu này
}