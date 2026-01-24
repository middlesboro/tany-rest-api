package sk.tany.rest.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sk.tany.rest.api.exception.BaseException;

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

}
