package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;

public class CartValidationException extends BaseException {

    public CartValidationException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
