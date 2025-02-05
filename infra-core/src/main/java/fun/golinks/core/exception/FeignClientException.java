package fun.golinks.core.exception;

public class FeignClientException extends Exception {

    public FeignClientException() {
        super();
    }

    public FeignClientException(String message) {
        super(message);
    }

    public FeignClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeignClientException(Throwable cause) {
        super(cause);
    }

    protected FeignClientException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
