package bsa.java.concurrency.image;

import bsa.java.concurrency.image.domain.Image;
import bsa.java.concurrency.image.dto.SearchResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private ImageService service;

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public void batchUploadImages(@RequestParam("images") MultipartFile[] files) {
        for (CompletableFuture<Image> imageCompletableFuture : service.uploadImages(files)) {
            try {
                imageCompletableFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        }
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<SearchResultDto> searchMatches(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "threshold",
                    defaultValue = "0.9") double threshold) {
        return service.searchImages(file, threshold);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable("id") UUID imageId) {
        service.deleteImageById(imageId);
    }

    @DeleteMapping("/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purgeImages() {
        service.deleteAllImages();
    }
}
