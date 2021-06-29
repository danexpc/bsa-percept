package bsa.java.concurrency.exception;

public class InvalidArgumentException extends RuntimeException {
    public InvalidArgumentException (String message, Throwable cause) {
        super(message, cause);
    }
}
