package bsa.java.concurrency.image.dto;

import bsa.java.concurrency.image.domain.Image;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ImageDto {
    private UUID id;

    private Long hash;

    private String path;

    public static ImageDto fromEntity(Image image) {
        return ImageDto.builder()
                .id(image.getId())
                .path(image.getPath())
                .hash(image.getHash())
                .build();
    }
}
