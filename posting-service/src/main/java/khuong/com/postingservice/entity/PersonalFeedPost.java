package khuong.com.postingservice.entity;

import khuong.com.postingservice.enums.PrivacySetting;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bai_dang_trang_ca_nhan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalFeedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bai_dang")
    private Long id;

    @CreationTimestamp
    @Column(name = "thoi_gian_dang", updatable = false)
    private LocalDateTime postedAt;

    @Lob
    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "quyen_rieng_tu", nullable = false, length = 20)
    private PrivacySetting privacy;

    @Column(name = "ma_nguoi_dung_dang_tai", nullable = false)
    private Long posterUserId; // ID từ Account-Service

    @OneToMany(mappedBy = "personalFeedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedImage> attachedImages;
}