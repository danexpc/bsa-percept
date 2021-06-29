package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.image.hash.DHasher;
import bsa.java.concurrency.image.hash.Hasher;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Log4j2
public class ImageService {

    @Autowired
    private FileSystemService fsService;

    @Autowired
    private ImageRepository repository;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final Hasher hasher;

    public ImageService(@Value("${application.image-hasher}") String hasherType) {
        if (hasherType.equals("dHash")) {
            this.hasher = new DHasher();
        } else {
            throw new InvalidPropertyException(ImageService.class, "application.image-hasher", "Unknown image hasher type");
        }
    }

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
        var hash = executor.submit(() -> hasher.calculateHash(bytes));
        var pathToImage = executor.submit(() -> fsService.saveFile(uuid, bytes));
        repository.save(
                Image.builder()
                        .id(uuid)
                        .hash(hash.get())
                        .path(pathToImage.get())
                        .build());
    }

    @SneakyThrows
    public List<SearchResultDTO> searchImages(MultipartFile file, double threshold) {
        var bytes = file.getBytes();
        var hash = hasher.calculateHash(bytes);
        var images = repository.findAllByHash(hash, threshold);
        if (!images.isEmpty()) {
            return images;
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

    public void deleteImageById(UUID imageId) {
        repository.deleteById(imageId);
        fsService.deleteFileByName(imageId);
    }

    public void deleteAllImages() {
        repository.deleteAll();
        fsService.deleteAllFiles();
    }
}
