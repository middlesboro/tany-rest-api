package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class HomepageGridException extends BaseException {

    public HomepageGridException(String message) {
        super(message);
    }

    public HomepageGridException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends HomepageGridException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends HomepageGridException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
