package bsa.java.concurrency.fs;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FileSystemService {

    @Autowired
    private FileSystem repository;

    @SneakyThrows
    public String saveFile(UUID id, byte[] file) {
        return repository.saveFile(id, file).get();
    }
}
