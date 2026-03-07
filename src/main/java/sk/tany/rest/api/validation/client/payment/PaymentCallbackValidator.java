package sk.tany.rest.api.validation.client.payment;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.client.payment.PaymentCallbackRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

@RequiredArgsConstructor
public class PaymentCallbackValidator implements ConstraintValidator<PaymentCallbackConstraint, PaymentCallbackRequest> {

    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(PaymentCallbackRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;

        if (isDangerous(request.getOperation())) { addViolation(context, "operation", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getOrderNumber())) { addViolation(context, "orderNumber", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getMerOrderNum())) { addViolation(context, "merOrderNum", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getMd())) { addViolation(context, "md", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getPrcode())) { addViolation(context, "prcode", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getSrcode())) { addViolation(context, "srcode", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getResultText())) { addViolation(context, "resultText", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getDigest())) { addViolation(context, "digest", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getDigest1())) { addViolation(context, "digest1", "contains dangerous content"); valid = false; }

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
