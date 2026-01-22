package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class CarrierException extends BaseException {

    public CarrierException(String message) {
        super(message);
    }

    public CarrierException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends CarrierException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends CarrierException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
