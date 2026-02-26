package sk.tany.rest.api.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.exception.CartValidationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartOrderValidatorTest {

    @Mock
    private CarrierRepository carrierRepository;

    @InjectMocks
    private CartOrderValidator validator;

    private CartDto createValidCart() {
        CartDto cart = new CartDto();
        cart.setFirstname("John");
        cart.setLastname("Doe");
        cart.setEmail("test@example.com");
        cart.setPhone("0901234567");
        cart.setSelectedCarrierId("carrier1");
        cart.setSelectedPaymentId("payment1");

        AddressDto address = new AddressDto("Street 1", "City", "12345", "Slovakia");
        cart.setDeliveryAddress(address);
        cart.setInvoiceAddress(address);

        return cart;
    }

    private Carrier createCarrier(CarrierType type) {
        Carrier c = new Carrier();
        c.setType(type);
        return c;
    }

    @Test
    void validate_validCart_shouldPass() {
        CartDto cart = createValidCart();
        when(carrierRepository.findById("carrier1")).thenReturn(Optional.of(createCarrier(CarrierType.COURIER)));

        assertDoesNotThrow(() -> validator.validate(cart, "Valid note"));
    }

    @Test
    void validate_missingFields_shouldFail() {
        CartDto cart = createValidCart();
        cart.setFirstname(null);
        assertThrows(CartValidationException.class, () -> validator.validate(cart, null));
    }

    @Test
    void validate_invalidEmail_shouldFail() {
        CartDto cart = createValidCart();
        cart.setEmail("invalid-email");
        assertThrows(CartValidationException.class, () -> validator.validate(cart, null));
    }

    @Test
    void validate_invalidPhone_shouldFail() {
        CartDto cart = createValidCart();
        cart.setPhone("123");
        assertThrows(CartValidationException.class, () -> validator.validate(cart, null));
    }

    @Test
    void validate_dangerousContent_shouldFail() {
        CartDto cart = createValidCart();
        cart.setFirstname("<script>alert(1)</script>");
        assertThrows(CartValidationException.class, () -> validator.validate(cart, null));
    }

    @Test
    void validate_dangerousNote_shouldFail() {
        CartDto cart = createValidCart();
        assertThrows(CartValidationException.class, () -> validator.validate(cart, "<script>"));
    }

    @Test
    void validate_packetaWithoutPickupPoint_shouldFail() {
        CartDto cart = createValidCart();
        when(carrierRepository.findById("carrier1")).thenReturn(Optional.of(createCarrier(CarrierType.PACKETA)));
        cart.setSelectedPickupPointId(null);

        assertThrows(CartValidationException.class, () -> validator.validate(cart, null));
    }

    @Test
    void validate_packetaWithPickupPoint_shouldPass() {
        CartDto cart = createValidCart();
        when(carrierRepository.findById("carrier1")).thenReturn(Optional.of(createCarrier(CarrierType.PACKETA)));
        cart.setSelectedPickupPointId("123");
        cart.setSelectedPickupPointName("Place");

        assertDoesNotThrow(() -> validator.validate(cart, null));
    }

    @Test
    void validate_missingAddress_shouldFail() {
        CartDto cart = createValidCart();
        cart.setDeliveryAddress(null);
        assertThrows(CartValidationException.class, () -> validator.validate(cart, null));
    }
}
