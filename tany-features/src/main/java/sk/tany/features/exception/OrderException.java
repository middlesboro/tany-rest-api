package sk.tany.features.exception;

import org.springframework.http.HttpStatus;

public class OrderException extends BaseException {

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends OrderException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends OrderException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
