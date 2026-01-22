package sk.tany.rest.api.exception;

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

    }

}
