package khuong.com.postingservice.dto;

import lombok.Data;

@Data
public class UserInfoShortResponse {
    private Long userId;
    private String fullName;
    private String avatarUrl;

}
