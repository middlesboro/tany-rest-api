package sk.tany.rest.api.validation.client.customer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.client.emailnotification.EmailNotificationCreateRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class EmailNotificationCreateValidator implements ConstraintValidator<EmailNotificationCreateConstraint, EmailNotificationCreateRequest> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");
    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(EmailNotificationCreateRequest request, ConstraintValidatorContext context) {
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
        }

        if (isBlank(request.getProductId())) {
            addViolation(context, "productId", "must not be blank");
            valid = false;
        } else if (!ID_PATTERN.matcher(request.getProductId()).matches()) {
            addViolation(context, "productId", "Invalid ID format");
            valid = false;
        } else if (isDangerous(request.getProductId())) {
            addViolation(context, "productId", "contains dangerous content");
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
