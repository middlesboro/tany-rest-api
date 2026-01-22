package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class FilterParameterException extends BaseException {

    public FilterParameterException(String message) {
        super(message);
    }

    public FilterParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends FilterParameterException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends FilterParameterException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
