package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class FilterParameterValueException extends BaseException {

    public FilterParameterValueException(String message) {
        super(message);
    }

    public FilterParameterValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends FilterParameterValueException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends FilterParameterValueException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
