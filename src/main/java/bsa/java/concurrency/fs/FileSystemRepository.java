package bsa.java.concurrency.fs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public class FileSystemRepository implements FileSystem {

    @Value("${application.resources.static-location}")
    private String pathToStorage;

    @Override
    public CompletableFuture<String> saveFile(UUID id, byte[] file) {
        return null;
    }
}
