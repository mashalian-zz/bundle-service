package se.seb.bundleservice.exception;

public class UnmatchedConditionsException extends RuntimeException {

    public UnmatchedConditionsException() {
        super();
    }

    public UnmatchedConditionsException(String message) {
        super(message);
    }

    public UnmatchedConditionsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmatchedConditionsException(Throwable cause) {
        super(cause);
    }

    public UnmatchedConditionsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
