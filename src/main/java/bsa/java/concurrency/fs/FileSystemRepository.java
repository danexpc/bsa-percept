package bsa.java.concurrency.fs;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public class FileSystemRepository implements FileSystem {

    private final Path pathToStorage;

    @SneakyThrows
    public FileSystemRepository(@Value("${application.resources.static-location}") String pathToStorage) {
        this.pathToStorage = Path.of(pathToStorage);
        if (!Files.exists(this.pathToStorage)) {
            Files.createDirectories(this.pathToStorage);
        }
    }


    @Override
    @SneakyThrows
    public CompletableFuture<String> saveFile(UUID name, byte[] file) {
        return CompletableFuture.supplyAsync(() -> supplySaveToFile(name, file));
    }

    @SneakyThrows
    private String supplySaveToFile(UUID name, byte[] file) {
        return Files.write(Path.of(pathToStorage.toString(), name + ".jpg"),
                file, StandardOpenOption.CREATE).toString();
    }
}
