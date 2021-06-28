package bsa.java.concurrency.image;

import bsa.java.concurrency.fs.FileSystemService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.UUID;

@Service
@Log4j2
public class ImageService {

    @Autowired
    private FileSystemService fsService;

    public void uploadImages(MultipartFile[] files) {
        Arrays.stream(files).forEach(this::uploadImage);
    }

    @SneakyThrows
    private void uploadImage(MultipartFile file) {
        var bytes = file.getBytes();
        var uuid = UUID.randomUUID();
        var pathToImage = fsService.saveFile(uuid, bytes);
        log.error(pathToImage);
    }
}
