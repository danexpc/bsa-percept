package bsa.java.concurrency.image;

import bsa.java.concurrency.image.dto.SearchResultDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    @Query(
            value = "select cast(id as varchar) as imageId, path as imageUrl, match as matchPercent " +
                    "from ( " +
                    "    select id, path, (1 - length(replace(cast(cast(hash as bit(10))#cast(:hash as bit(10)) as text), '0', '')) / 64.0) as match " +
                    "    from images ) imagesWithMatch " +
                    "where match >= :threshold ",
            nativeQuery = true)
    List<SearchResultDTO> findAllByHash(long hash, double threshold);
}
