package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class CategoryException extends BaseException {

    public CategoryException(String message) {
        super(message);
    }

    public CategoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends CategoryException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends CategoryException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
