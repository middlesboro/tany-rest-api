package sk.tany.rest.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sk.tany.rest.api.exception.AuthenticationException;
import sk.tany.rest.api.exception.BaseException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerControllerAdvice {

    @ExceptionHandler({
            BaseException.class,
    })
    public ResponseEntity<ErrorResponse> handleUnexpectedExceptions(Throwable ex) {
        log.error(ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse error = new ErrorResponseException(status, ProblemDetail.forStatus(status.value()), ex);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler({
            AuthenticationException.InvalidToken.class,
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(Throwable ex) {
        log.error(ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponseException(HttpStatus.UNAUTHORIZED, ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value()), ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}
