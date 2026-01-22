package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class PageContentException extends BaseException {

    public PageContentException(String message) {
        super(message);
    }

    public PageContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends PageContentException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends PageContentException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
