package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.image.hash.DHasher;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final DHasher hasher = new DHasher();

    public void uploadImages(List<byte[]> files) {
        files.forEach(file -> executor.execute(() -> this.uploadImage(file)));
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
    public List<SearchResultDTO> searchImages(byte[] file, double threshold) {
        var hash = hasher.calculateHash(file);
        var images = repository.findAllByHash(hash, threshold);
        if (!images.isEmpty()) {
            return images;
        }

        executor.execute(() -> saveImage(file, hash));

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
