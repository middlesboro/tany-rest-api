package sk.tany.rest.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sk.tany.rest.api.exception.AuthenticationException;
import sk.tany.rest.api.exception.BaseException;
import sk.tany.rest.api.exception.CartDiscountException;
import sk.tany.rest.api.exception.CartException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerControllerAdvice {

    @ExceptionHandler({
            BaseException.class,
    })
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.error(ex.getMessage(), ex);

        HttpStatus status = ex.getHttpStatus();
        ErrorResponse error = new ErrorResponseException(status, ProblemDetail.forStatus(status.value()), ex);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
    })
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentTypeMismatchException ex) {
        log.error(ex.getMessage(), ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = new ErrorResponseException(status, ProblemDetail.forStatus(status.value()), ex);
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = new ErrorResponseException(status, ProblemDetail.forStatus(status.value()), ex);
        return new ResponseEntity<>(error, status);
    }

    // todo add all not found exceptions
    @ExceptionHandler({
            CartException.NotFound.class,
            CartDiscountException.NotFound.class,
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            AuthorizationDeniedException.class,
    })
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            AuthenticationException.class,
    })
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            Exception.class,
    })
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
