package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class EmailNotificationException extends BaseException {

    public EmailNotificationException(String message) {
        super(message);
    }

    public EmailNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class NotFound extends EmailNotificationException {
        public NotFound(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.NOT_FOUND;
        }
    }

    public static final class Conflict extends EmailNotificationException {
        public Conflict(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.CONFLICT;
        }
    }
}
