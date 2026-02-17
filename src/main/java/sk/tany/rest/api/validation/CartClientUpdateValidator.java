package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest.CartItem;

import java.util.regex.Pattern;

public class CartClientUpdateValidator implements ConstraintValidator<CartClientUpdateConstraint, CartClientUpdateRequest> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SK_PHONE_PATTERN = Pattern.compile("^(\\+421|0)?9\\d{8}$");
    private static final Pattern DANGEROUS_HTML_PATTERN = Pattern.compile("(?i)<script|javascript:|onload|onerror|<iframe>|<object>|<embed>|<link>|<style>|<meta>");

    @Override
    public boolean isValid(CartClientUpdateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;
        // Disable default violation initially, we will add specific ones if needed.
        // But we only want to disable it if we actually add custom violations.
        // A common pattern is to disable it only when we find an error.

        // 1. cartId
        if (isBlank(request.getCartId())) {
            addViolation(context, "cartId", "must not be blank");
            valid = false;
        } else if (isDangerous(request.getCartId())) {
            addViolation(context, "cartId", "contains dangerous content");
            valid = false;
        }

        // 2. Items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (int i = 0; i < request.getItems().size(); i++) {
                CartItem item = request.getItems().get(i);
                if (isBlank(item.getProductId())) {
                    addViolation(context, "items[" + i + "].productId", "must not be blank");
                    valid = false;
                } else if (isDangerous(item.getProductId())) {
                    addViolation(context, "items[" + i + "].productId", "contains dangerous content");
                    valid = false;
                }
                if (item.getQuantity() == null || item.getQuantity() < 1) {
                    addViolation(context, "items[" + i + "].quantity", "must be greater than or equal to 1");
                    valid = false;
                }
                if (isDangerous(item.getTitle())) {
                     addViolation(context, "items[" + i + "].title", "contains dangerous content");
                     valid = false;
                }
                if (isDangerous(item.getImage())) {
                     addViolation(context, "items[" + i + "].image", "contains dangerous content");
                     valid = false;
                }
            }
        }

        // 3. Email
        if (!isBlank(request.getEmail())) {
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                addViolation(context, "email", "must be a valid email address");
                valid = false;
            }
        }

        // 4. Phone
        if (!isBlank(request.getPhone())) {
            if (!SK_PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                addViolation(context, "phone", "must be a valid Slovak phone number");
                valid = false;
            }
        }

        // 5. Addresses
        if (!validateAddress(request.getInvoiceAddress(), "invoiceAddress", context)) {
            valid = false;
        }
        if (!validateAddress(request.getDeliveryAddress(), "deliveryAddress", context)) {
            valid = false;
        }

        // 6. Global XSS
        if (isDangerous(request.getFirstname())) { addViolation(context, "firstname", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getLastname())) { addViolation(context, "lastname", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getCustomerId())) { addViolation(context, "customerId", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getSelectedCarrierId())) { addViolation(context, "selectedCarrierId", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getSelectedPaymentId())) { addViolation(context, "selectedPaymentId", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getSelectedPickupPointId())) { addViolation(context, "selectedPickupPointId", "contains dangerous content"); valid = false; }
        if (isDangerous(request.getSelectedPickupPointName())) { addViolation(context, "selectedPickupPointName", "contains dangerous content"); valid = false; }

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
        return DANGEROUS_HTML_PATTERN.matcher(value).find();
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
