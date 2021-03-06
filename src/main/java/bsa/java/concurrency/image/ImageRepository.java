package bsa.java.concurrency.image;

import bsa.java.concurrency.image.domain.Image;
import bsa.java.concurrency.image.domain.SearchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    @Query(
            value = "select cast(id as varchar) as imageId, path as imageUrl, match as matchPercent " +
                    "from ( " +
                    "    select id, path, (1 - length(replace(cast(cast(hash as bit(64))#cast(:hash as bit(64)) as text), '0', '')) / 64.0) as match " +
                    "    from images ) imagesWithMatch " +
                    "where match >= :threshold ",
            nativeQuery = true)
    List<SearchResult> findAllByHash(long hash, double threshold);

    @Query(
            value = "delete from images i where i.id = :id",
            nativeQuery = true
    )
    @Modifying
    void deleteById(UUID id);
}
