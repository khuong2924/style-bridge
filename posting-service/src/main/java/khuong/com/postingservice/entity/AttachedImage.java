package khuong.com.postingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hinh_anh_dinh_kem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hinh_anh")
    private Long id;

    @Column(name = "duong_dan_luu_tru", nullable = false, length = 255)
    private String storagePath; // URL của ảnh

    @Column(name = "thu_tu_trong_bo_anh")
    private Integer orderInAlbum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_dang_tuyen", nullable = true)
    private RecruitmentPost recruitmentPost; // Ảnh cho bài đăng tuyển

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_yeu_cau", nullable = true)
    private ApplicationRequest applicationRequest; // Ảnh cho yêu cầu ứng tuyển

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bai_dang_trang_ca_nhan", nullable = true)
    private PersonalFeedPost personalFeedPost; // Ảnh cho bài đăng cá nhân
}