package bsa.java.concurrency.fs;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/files")
public class FilesController {

    @Autowired
    private FileSystemService service;

    @GetMapping(
            value = "/{name}",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getFile(@PathVariable String name) {
        return service.getFileByName(name);
    }


}
