package sk.tany.rest.api.validation.client.cart;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CartClientSetCarrierValidator implements ConstraintValidator<CartClientSetCarrierConstraint, CartClientSetCarrierRequest> {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");
    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(CartClientSetCarrierRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;

        if (isBlank(request.getCartId())) {
            addViolation(context, "cartId", "must not be blank");
            valid = false;
        } else if (!ID_PATTERN.matcher(request.getCartId()).matches()) {
            addViolation(context, "cartId", "Invalid ID format");
            valid = false;
        } else if (isDangerous(request.getCartId())) {
            addViolation(context, "cartId", "contains dangerous content");
            valid = false;
        }

        if (isBlank(request.getCarrierId())) {
            addViolation(context, "carrierId", "must not be blank");
            valid = false;
        } else if (!ID_PATTERN.matcher(request.getCarrierId()).matches()) {
            addViolation(context, "carrierId", "Invalid ID format");
            valid = false;
        } else if (isDangerous(request.getCarrierId())) {
            addViolation(context, "carrierId", "contains dangerous content");
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
