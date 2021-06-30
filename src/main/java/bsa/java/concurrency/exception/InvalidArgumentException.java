package bsa.java.concurrency.exception;

public final class InvalidArgumentException extends RuntimeException {
    public InvalidArgumentException (String message, Throwable cause) {
        super(message, cause);
    }
}
