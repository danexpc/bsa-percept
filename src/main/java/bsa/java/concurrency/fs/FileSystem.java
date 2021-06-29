package bsa.java.concurrency.fs;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FileSystem {
    CompletableFuture<byte[]> getByName(String name);

    CompletableFuture<String> saveFile(UUID id, byte[] file);

    void deleteByName(UUID name);

    void deleteAll();
}
