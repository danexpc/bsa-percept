package bsa.java.concurrency.fs;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileSystemService {

    @Autowired
    private FileSystem repository;

    public byte[] getFileByName(String name) {
        return repository.getByName(name);
    }

    @SneakyThrows
    public String saveFile(UUID name, byte[] file) {
        return repository.saveFile(name, file).get();
    }

    public void deleteFileByName(UUID name) {
        repository.deleteByName(name);
    }

    public void deleteAllFiles() {
        repository.deleteAll();
    }
}
