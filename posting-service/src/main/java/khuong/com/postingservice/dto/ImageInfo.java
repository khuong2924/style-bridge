package khuong.com.postingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfo {
    private Long id;
    private String storagePath;
    private Integer orderInAlbum;
}