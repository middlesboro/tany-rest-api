package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class ShopSettingsException extends BaseException {

    public ShopSettingsException(String message) {
        super(message);
    }

    public ShopSettingsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends ShopSettingsException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class BadRequest extends ShopSettingsException {
        public BadRequest(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }
}
