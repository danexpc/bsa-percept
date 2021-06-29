package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.image.domain.Image;
import bsa.java.concurrency.image.dto.SearchResultDto;
import bsa.java.concurrency.image.hash.Hasher;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ImageService {

    @Autowired
    private FileSystemService fsService;

    @Autowired
    private ImageRepository repository;

    @Autowired
    private Map<String, Hasher> hasherMap;

    @Value("${application.image-hasher}")
    private String defaultHasher;

    @Autowired
    HttpServletRequest request;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @SneakyThrows
    public void uploadImages(MultipartFile[] files) {
        Arrays.stream(files).map(file -> {
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }).forEach(file -> executor.execute(() -> this.uploadImage(file)));
    }

    @SneakyThrows
    private void uploadImage(byte[] bytes) {
        var uuid = UUID.randomUUID();
        var hash = executor.submit(() -> {
            var hasher = request.getParameter("hasher");

            if (hasher == null) {
                return hasherMap.get(defaultHasher).calculateHash(bytes);
            } else if (hasherMap.containsKey(hasher)) {
                return hasherMap.get(hasher).calculateHash(bytes);
            }

            throw new UnsupportedOperationException();
        });
        var pathToImage = executor.submit(() -> fsService.saveFile(uuid, bytes));
        repository.save(
                Image.builder()
                        .id(uuid)
                        .hash(hash.get())
                        .path(pathToImage.get())
                        .build());
    }

    @SneakyThrows
    public List<SearchResultDto> searchImages(MultipartFile file, double threshold) {
        var bytes = file.getBytes();
        long hash;
        var hasher = request.getParameter("hasher");
        if (hasher == null) {
            hash =  hasherMap.get(defaultHasher).calculateHash(bytes);
        } else if (hasherMap.containsKey(hasher)) {
            hash = hasherMap.get(hasher).calculateHash(bytes);
        } else {
            throw new UnsupportedOperationException();
        }
        var images = repository.findAllByHash(hash, threshold);
        if (!images.isEmpty()) {
            return images.stream()
                    .map(image -> new SearchResultDto(
                            image.getImageId(),
                            image.getMatchPercent(),
                            (ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/" +
                                    image.getImageUrl().replace("\\", "/"))))
                    .collect(Collectors.toList());
        }

        executor.execute(() -> saveImage(bytes, hash));

        return List.of();
    }

    @SneakyThrows
    private void saveImage(byte[] bytes, long hash) {
        var uuid = UUID.randomUUID();
        var pathToImage = fsService.saveFile(uuid, bytes);
        repository.save(
                Image.builder()
                        .id(uuid)
                        .hash(hash)
                        .path(pathToImage)
                        .build());
    }

    @Transactional
    public void deleteImageById(UUID imageId) {
        repository.deleteById(imageId);
        fsService.deleteFileByName(imageId);
    }

    @Transactional
    public void deleteAllImages() {
        repository.deleteAll();
        fsService.deleteAllFiles();
    }
}
