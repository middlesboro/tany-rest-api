package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final class InvalidToken extends AuthenticationException {

        public InvalidToken(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.UNAUTHORIZED;
        }

    }

    public static final class AuthorizationFailed extends AuthenticationException {
        public AuthorizationFailed(String message) {
            super(message);
        }

        @Override
        public HttpStatus getHttpStatus() {
            return HttpStatus.BAD_REQUEST;
        }
    }

}
