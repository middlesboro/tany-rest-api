package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

@RequiredArgsConstructor
public class ReviewClientCreateValidator implements ConstraintValidator<ReviewClientCreateConstraint, ReviewClientCreateRequest> {

    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(ReviewClientCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;

        if (isDangerous(request.getProductId())) {
            addViolation(context, "productId", "contains dangerous content");
            valid = false;
        }
        if (isDangerous(request.getText())) {
            addViolation(context, "text", "contains dangerous content");
            valid = false;
        }
        if (isDangerous(request.getTitle())) {
            addViolation(context, "title", "contains dangerous content");
            valid = false;
        }
        if (isDangerous(request.getEmail())) {
            addViolation(context, "email", "contains dangerous content");
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

    private void addViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(property)
               .addConstraintViolation();
    }
}
