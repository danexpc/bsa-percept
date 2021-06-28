package bsa.java.concurrency.image;

import bsa.java.concurrency.image.dto.SearchResultDTO;
import lombok.SneakyThrows;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private ImageService service;

    @SneakyThrows
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public void batchUploadImages(@RequestParam("images") MultipartFile[] files) {
        List<byte[]> list = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] bytes = file.getBytes();
            list.add(bytes);
        }
        service.uploadImages(list);
    }

    @SneakyThrows
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<SearchResultDTO> searchMatches(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "threshold",
                    defaultValue = "0.9") double threshold) {
        return service.searchImages(file.getBytes(), threshold);
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
