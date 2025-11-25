package exception;

public class GoogleLoginException extends RuntimeException {
    public GoogleLoginException(String message) {
        super(message);
    }

    public GoogleLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}