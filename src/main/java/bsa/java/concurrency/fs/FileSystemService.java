package bsa.java.concurrency.fs;

import bsa.java.concurrency.exception.InvalidArgumentException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class FileSystemService {

    @Autowired
    private FileSystem repository;

    @SneakyThrows
    public byte[] getFileByName(String name) {
        try {
            return repository.getByName(name).get();
        } catch (ExecutionException e) {
            throw new InvalidArgumentException(String.format("File with name: %s cannot be read", name), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedException(String.format("Getting file with name: %s was interrupted", name));
        }
    }

    @SneakyThrows
    public String saveFile(UUID name, byte[] file) {
        try {
            return repository.saveFile(name, file).get();
        } catch (ExecutionException e) {
            throw new InvalidArgumentException(String.format("File with name: %s cannot be created", name), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedException(String.format("Saving file with name: %s was interrupted", name));
        }
    }

    public void deleteFileByName(UUID name) {
        try {
            repository.deleteByName(name);
        } catch (RuntimeException e) {
            throw new InvalidArgumentException(String.format("File with name: %s cannot be deleted", name), e.getCause());
        }

    }

    public void deleteAllFiles() {
        repository.deleteAll();
    }
}
