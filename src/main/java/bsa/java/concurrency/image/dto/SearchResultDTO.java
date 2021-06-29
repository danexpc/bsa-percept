package bsa.java.concurrency.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SearchResultDto {
    UUID imageId;
    Double matchPercent;
    String imageUrl;
}
