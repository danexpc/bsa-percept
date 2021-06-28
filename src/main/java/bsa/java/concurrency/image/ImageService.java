package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystemService;
import bsa.java.concurrency.image.dto.SearchResultDTO;
import bsa.java.concurrency.image.hash.DHasher;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class ImageService {

    @Autowired
    private FileSystemService fsService;

    @Autowired
    private ImageRepository repository;

    private final DHasher hasher = new DHasher();

    public void uploadImages(MultipartFile[] files) {
        Arrays.stream(files).forEach(this::uploadImage);
    }

    @SneakyThrows
    private void uploadImage(MultipartFile file) {
        var bytes = file.getBytes();
        var uuid = UUID.randomUUID();
        var pathToImage = fsService.saveFile(uuid, bytes);
        var hash = hasher.calculateHash(bytes);
        repository.save(
                Image.builder()
                        .id(uuid)
                        .hash(hash)
                        .path(pathToImage)
                        .build());
    }

    @SneakyThrows
    public List<SearchResultDTO> searchImages(MultipartFile file, double threshold) {
        var hash = hasher.calculateHash(file.getBytes());
        var images = repository.findAllByHash(hash, threshold);
        if (!images.isEmpty()) {
            return images;
        }

        uploadImage(file);
        return List.of();
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
