package khuong.com.postingservice.entity;

import khuong.com.postingservice.enums.PrivacySetting;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Lob
    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "caption", length = 255)
    private String caption;
    
    @Column(name = "tags", length = 255)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "quyen_rieng_tu", nullable = false, length = 20)
    private PrivacySetting privacy;

    @Column(name = "ma_nguoi_dung_dang_tai", nullable = false)
    private Long posterUserId; // ID từ Account-Service

    @Column(name = "thoi_gian_dang", nullable = false)
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "personalFeedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedImage> attachedImages;
}