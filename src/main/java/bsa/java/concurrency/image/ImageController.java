package bsa.java.concurrency.image;

import bsa.java.concurrency.image.dto.ImageDto;
import bsa.java.concurrency.image.dto.SearchResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private ImageService service;

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ImageDto> batchUploadImages(@RequestParam("images") MultipartFile[] files) {
        List<ImageDto> list = new ArrayList<>();
        for (Future<ImageDto> imageDtoFuture : service.uploadImages(files)) {
            ImageDto imageDto;
            try {
                imageDto = imageDtoFuture.get();
                imageDto.setPath(service.createUrlToImage(imageDto.getPath()));
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
            list.add(imageDto);
        }
        return list;
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
