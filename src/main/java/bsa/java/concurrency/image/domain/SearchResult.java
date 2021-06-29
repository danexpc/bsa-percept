package bsa.java.concurrency.image.domain;

import java.util.UUID;

public interface SearchResult {
    UUID getImageId();

    Double getMatchPercent();

    String getImageUrl();
}
