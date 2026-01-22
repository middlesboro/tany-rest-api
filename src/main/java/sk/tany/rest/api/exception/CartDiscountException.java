package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class CartDiscountException extends BaseException {

    public CartDiscountException(String message) {
        super(message);
    }

    public CartDiscountException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends CartDiscountException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends CartDiscountException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
