package bsa.java.concurrency.fs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
