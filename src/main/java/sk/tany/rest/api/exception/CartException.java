package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class CartException extends BaseException {

    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends CartException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends CartException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
