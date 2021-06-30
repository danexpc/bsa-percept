package bsa.java.concurrency.exception;

public final class UnavailableResourceException extends RuntimeException {
    public UnavailableResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
