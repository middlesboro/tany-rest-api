package sk.tany.rest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ContentSnippetException extends RuntimeException {

    public ContentSnippetException(String message) {
        super(message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends ContentSnippetException {
        public NotFound(String message) {
            super(message);
        }
    }
}
