package khuong.com.postingservice.entity;

import khuong.com.postingservice.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Lob
    @Column(name = "loi_nhan", columnDefinition = "TEXT")
    private String message;

    @Column(name = "thong_tin_lien_lac", length = 255)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 20)
    private ApplicationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_dang_tuyen", nullable = false)
    private RecruitmentPost recruitmentPost;

    @Column(name = "ma_nguoi_dung_ung_tuyen", nullable = false)
    private Long applicantUserId;

    @OneToMany(mappedBy = "applicationRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedImage> attachedImages;
}