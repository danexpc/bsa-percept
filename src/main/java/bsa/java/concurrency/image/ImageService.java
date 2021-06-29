package bsa.java.concurrency.image;

import bsa.java.concurrency.exception.InvalidArgumentException;
import bsa.java.concurrency.exception.UnavailableResourceException;
import bsa.java.concurrency.exception.UnsupportedHasherException;
import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.image.domain.Image;
import bsa.java.concurrency.image.dto.ImageDto;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    @Value("${application.default-hasher}")
    private String defaultHasher;

    @Autowired
    HttpServletRequest request;

    private final ExecutorService executor;

    public ImageService(@Value("#{new Integer('${application.thread-pool-size}')}") Integer poolSize) {
        executor = Executors.newFixedThreadPool(poolSize);
    }

    public List<CompletableFuture<ImageDto>> uploadImages(MultipartFile[] files) {
        var hasher = request.getParameter("hasher");
        return Arrays.stream(files)
                .map(file -> {
                    try {
                        return file.getBytes();
                    } catch (IOException e) {
                        throw new UnavailableResourceException("Resource is not available", e.getCause());
                    }
                })
                .map(file -> executor.submit(() -> uploadImage(file, hasher)))
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        throw new InvalidArgumentException("Exception during processing resource", e.getCause());
                    }
                })
                .collect(Collectors.toList());
    }

    private CompletableFuture<ImageDto> uploadImage(byte[] bytes, String hasher) {
        var uuid = UUID.randomUUID();
        var hash = executor.submit(() -> {
            if (hasher == null) {
                return hasherMap.get(defaultHasher).calculateHash(bytes);
            } else if (hasherMap.containsKey(hasher)) {
                return hasherMap.get(hasher).calculateHash(bytes);
            }
            throw new UnsupportedHasherException(String.format("Hasher with name: %s not found", hasher));
        });
        var pathToImage = executor.submit(() -> fsService.saveFile(uuid, bytes));
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return ImageDto.fromEntity(repository.save(
                                Image.builder()
                                        .id(uuid)
                                        .hash(hash.get())
                                        .path(pathToImage.get())
                                        .build()));
                    } catch (InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        throw new InvalidArgumentException("Exception during processing resource", e.getCause());
                    }
                });
    }

    public List<SearchResultDto> searchImages(MultipartFile file, double threshold) {
        try {
            var bytes = file.getBytes();
            long hash;
            var hasher = request.getParameter("hasher");
            if (hasher == null) {
                hash = hasherMap.get(defaultHasher).calculateHash(bytes);
            } else if (hasherMap.containsKey(hasher)) {
                hash = hasherMap.get(hasher).calculateHash(bytes);
            } else {
                throw new UnsupportedHasherException(String.format("Hasher with name: %s not found", hasher));
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
        } catch (IOException e) {
            throw new UnavailableResourceException("Resource is not available", e.getCause());
        }
    }

    private void saveImage(byte[] bytes, long hash) {
        try {
            var uuid = UUID.randomUUID();
            var pathToImage = fsService.saveFile(uuid, bytes);
            repository.save(
                    Image.builder()
                            .id(uuid)
                            .hash(hash)
                            .path(pathToImage)
                            .build());
        } catch (RuntimeException e) {
            throw new InvalidArgumentException("Entity cannot be created", e.getCause());
        }
    }

    @Transactional
    public void deleteImageById(UUID imageId) {
        try {
            repository.deleteById(imageId);
            fsService.deleteFileByName(imageId);
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException(String.format("Image with id: %s cannot be deleted", imageId), e.getCause());
        }

    }

    @Transactional
    public void deleteAllImages() {
        repository.deleteAll();
        fsService.deleteAllFiles();
    }
}
