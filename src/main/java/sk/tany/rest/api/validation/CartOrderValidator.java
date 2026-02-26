package sk.tany.rest.api.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.exception.CartValidationException;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CartOrderValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SK_PHONE_PATTERN = Pattern.compile("^(\\+421|0)?9\\d{8}$");
    private static final Pattern DANGEROUS_HTML_PATTERN = Pattern.compile("(?i)<script|javascript:|onload|onerror|<iframe>|<object>|<embed>|<link>|<style>|<meta>");

    private final CarrierRepository carrierRepository;

    public void validate(CartDto cart, String note) {
        if (cart == null) {
            throw new CartValidationException("Cart is missing");
        }

        // 1. Required fields
        if (isBlank(cart.getFirstname())) throw new CartValidationException("Firstname is required");
        if (isBlank(cart.getLastname())) throw new CartValidationException("Lastname is required");
        if (isBlank(cart.getEmail())) throw new CartValidationException("Email is required");
        if (isBlank(cart.getPhone())) throw new CartValidationException("Phone is required");
        if (isBlank(cart.getSelectedCarrierId())) throw new CartValidationException("Carrier is required");
        if (isBlank(cart.getSelectedPaymentId())) throw new CartValidationException("Payment is required");

        // 2. Format
        if (!EMAIL_PATTERN.matcher(cart.getEmail()).matches()) {
            throw new CartValidationException("Invalid email format");
        }
        if (!SK_PHONE_PATTERN.matcher(cart.getPhone()).matches()) {
            throw new CartValidationException("Invalid phone format (must be Slovak number)");
        }

        // 3. Dangerous content
        if (isDangerous(cart.getFirstname())) throw new CartValidationException("Firstname contains dangerous content");
        if (isDangerous(cart.getLastname())) throw new CartValidationException("Lastname contains dangerous content");
        if (isDangerous(cart.getEmail())) throw new CartValidationException("Email contains dangerous content");
        if (isDangerous(cart.getPhone())) throw new CartValidationException("Phone contains dangerous content");
        if (isDangerous(cart.getSelectedPickupPointId())) throw new CartValidationException("Pickup point ID contains dangerous content");
        if (isDangerous(cart.getSelectedPickupPointName())) throw new CartValidationException("Pickup point name contains dangerous content");
        if (isDangerous(note)) throw new CartValidationException("Note contains dangerous content");

        // 4. Address validation
        validateAddress(cart.getDeliveryAddress(), "Delivery address");
        validateAddress(cart.getInvoiceAddress(), "Invoice address");

        // 5. Carrier specific validation
        Optional<Carrier> carrierOpt = carrierRepository.findById(cart.getSelectedCarrierId());
        if (carrierOpt.isPresent()) {
            Carrier carrier = carrierOpt.get();
            if (CarrierType.PACKETA == carrier.getType() || CarrierType.BALIKOVO == carrier.getType()) {
                if (isBlank(cart.getSelectedPickupPointId())) {
                    throw new CartValidationException("Pickup point is required for selected carrier");
                }
                if (isBlank(cart.getSelectedPickupPointName())) {
                    throw new CartValidationException("Pickup point name is required for selected carrier");
                }
            }
        } else {
             // Should we fail if carrier not found? Typically yes, but maybe it's handled elsewhere.
             // Given strictly validating cart for order creation, yes.
             throw new CartValidationException("Selected carrier not found");
        }
    }

    private void validateAddress(AddressDto address, String addressType) {
        if (address == null) {
            throw new CartValidationException(addressType + " is required");
        }
        if (isBlank(address.getStreet())) throw new CartValidationException(addressType + " street is required");
        if (isBlank(address.getCity())) throw new CartValidationException(addressType + " city is required");
        if (isBlank(address.getZip())) throw new CartValidationException(addressType + " zip is required");
        if (isBlank(address.getCountry())) throw new CartValidationException(addressType + " country is required");

        if (isDangerous(address.getStreet())) throw new CartValidationException(addressType + " street contains dangerous content");
        if (isDangerous(address.getCity())) throw new CartValidationException(addressType + " city contains dangerous content");
        if (isDangerous(address.getZip())) throw new CartValidationException(addressType + " zip contains dangerous content");
        if (isDangerous(address.getCountry())) throw new CartValidationException(addressType + " country contains dangerous content");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isDangerous(String value) {
        if (value == null || value.isBlank()) return false;
        return DANGEROUS_HTML_PATTERN.matcher(value).find();
    }
}
