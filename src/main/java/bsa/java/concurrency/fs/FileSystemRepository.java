package bsa.java.concurrency.fs;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public class FileSystemRepository implements FileSystem {

    private final Path pathToStorage;

    @Value("${application.file.extensions}")
    private String fileExtension;

    @SneakyThrows
    public FileSystemRepository(@Value("${application.resources.static-location}") String pathToStorage) {
        this.pathToStorage = Path.of(pathToStorage);
        if (!Files.exists(this.pathToStorage)) {
            Files.createDirectories(this.pathToStorage);
        }
    }

    @Override
    @SneakyThrows
    public CompletableFuture<byte[]> getByName(String name) {
        return CompletableFuture.supplyAsync(() -> supplyGetByName(name));
    }

    @Override
    @SneakyThrows
    public CompletableFuture<String> saveFile(UUID name, byte[] file) {
        return CompletableFuture.supplyAsync(() -> supplySaveToFile(name, file));
    }

    @Override
    @SneakyThrows
    public void deleteByName(UUID name) {
        Files.delete(Path.of(pathToStorage.toString(), name + fileExtension));
    }


    @Override
    @SneakyThrows
    public void deleteAll() {
        try (var paths = Files.newDirectoryStream(pathToStorage)) {
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    @SneakyThrows
    private String supplySaveToFile(UUID name, byte[] file) {
        return Files.write(Path.of(pathToStorage.toString(), name + fileExtension),
                file, StandardOpenOption.CREATE).toString();
    }

    @SneakyThrows
    private byte[] supplyGetByName(String name) {
        return Files.readAllBytes(Path.of(pathToStorage.toString(), name));
    }
}
