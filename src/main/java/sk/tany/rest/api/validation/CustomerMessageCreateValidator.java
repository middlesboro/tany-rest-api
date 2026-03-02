package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.client.customermessage.CustomerMessageCreateRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CustomerMessageCreateValidator implements ConstraintValidator<CustomerMessageCreateConstraint, CustomerMessageCreateRequest> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(CustomerMessageCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;

        if (isBlank(request.getEmail())) {
            addViolation(context, "email", "must not be blank");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            addViolation(context, "email", "must be a valid email address");
            valid = false;
        } else if (isDangerous(request.getEmail())) {
            addViolation(context, "email", "contains dangerous content");
            valid = false;
        }

        if (isBlank(request.getMessage())) {
            addViolation(context, "message", "must not be blank");
            valid = false;
        } else if (isDangerous(request.getMessage())) {
            addViolation(context, "message", "contains dangerous content");
            valid = false;
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
        }

        return valid;
    }

    private boolean isDangerous(String value) {
        if (value == null || value.isBlank()) return false;
        String sanitized = htmlSanitizerService.sanitize(value);
        return !value.equals(Parser.unescapeEntities(sanitized, false));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void addViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(property)
               .addConstraintViolation();
    }
}
