package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.service.HtmlSanitizerService;

@RequiredArgsConstructor
public class SafeStringValidator implements ConstraintValidator<SafeString, String> {

    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String sanitized = htmlSanitizerService.sanitize(value);
        return value.equals(Parser.unescapeEntities(sanitized, false));
    }
}
