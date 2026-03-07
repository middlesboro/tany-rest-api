package sk.tany.rest.api.validation.client.customer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CustomerClientUpdateValidator implements ConstraintValidator<CustomerClientUpdateConstraint, CustomerClientUpdateRequest> {

    private static final Pattern SK_PHONE_PATTERN = Pattern.compile("^(\\+421|0)?9\\d{8}$");
    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(CustomerClientUpdateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;

        if (!isBlank(request.getPhone())) {
            if (!SK_PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                addViolation(context, "phone", "must be a valid Slovak phone number");
                valid = false;
            }
        }

        if (isDangerous(request.getFirstname())) { addViolation(context, "firstname", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getLastname())) { addViolation(context, "lastname", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getPreferredPacketaBranchId())) { addViolation(context, "preferredPacketaBranchId", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getPreferredPacketaBranchName())) { addViolation(context, "preferredPacketaBranchName", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getPreferredBalikovoBranchId())) { addViolation(context, "preferredBalikovoBranchId", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getPreferredBalikovoBranchName())) { addViolation(context, "preferredBalikovoBranchName", "contains dangerous content"); valid = false; }

        if (!validateAddress(request.getInvoiceAddress(), "invoiceAddress", context)) {
            valid = false;
        }
        if (!validateAddress(request.getDeliveryAddress(), "deliveryAddress", context)) {
            valid = false;
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
        }

        return valid;
    }

    private boolean validateAddress(AddressDto address, String fieldName, ConstraintValidatorContext context) {
        if (address == null) return true;
        boolean valid = true;

        if (isDangerous(address.getStreet())) {
            addViolation(context, fieldName + ".street", "contains dangerous content");
            valid = false;
        }
        if (isDangerous(address.getCity())) {
            addViolation(context, fieldName + ".city", "contains dangerous content");
            valid = false;
        }
        if (isDangerous(address.getZip())) {
            addViolation(context, fieldName + ".zip", "contains dangerous content");
            valid = false;
        }
        if (isDangerous(address.getCountry())) {
            addViolation(context, fieldName + ".country", "contains dangerous content");
            valid = false;
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
