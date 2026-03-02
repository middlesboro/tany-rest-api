package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest.CartItem;
import sk.tany.rest.api.service.HtmlSanitizerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartClientUpdateValidatorTest {

    @InjectMocks
    private CartClientUpdateValidator validator;

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;
    @Mock
    private HtmlSanitizerService htmlSanitizerService;

    @BeforeEach
    void setUp() {
        lenient().when(htmlSanitizerService.sanitize(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        lenient().when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenRequestIsValid() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");
        request.setEmail("test@example.com");
        request.setPhone("+421905123456");

        CartItem item = new CartItem("prod-1", 1);
        request.setItems(List.of(item));

        AddressDto address = new AddressDto("Street", "City", "Zip", "Country");
        request.setInvoiceAddress(address);

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenCartIdIsBlank() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
        verify(builder).addPropertyNode("cartId");
    }

    @Test
    void isValid_ShouldReturnFalse_WhenItemIsInvalid() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");

        CartItem item = new CartItem("", 0); // Invalid productId and quantity
        request.setItems(List.of(item));

        boolean result = validator.isValid(request, context);

        assertFalse(result);
        verify(context, atLeastOnce()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenEmailIsInvalid() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");
        request.setEmail("invalid-email");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPhoneIsInvalid() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");
        request.setPhone("123"); // Not Slovak format

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenAddressFieldsAreEmpty() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");
        AddressDto address = new AddressDto("", "", "", ""); // Empty fields
        request.setInvoiceAddress(address);

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenAddressContainsXSS() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");
        String dangerous = "<script>alert(1)</script>";
        AddressDto address = new AddressDto(dangerous, "City", "Zip", "Country");
        request.setInvoiceAddress(address);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenGlobalFieldContainsXSS() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");
        String dangerous = "<script>alert(1)</script>";
        request.setFirstname(dangerous);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenCartIdContainsXSS() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        String dangerous = "<script>alert(1)</script>";
        request.setCartId(dangerous);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenProductIdContainsXSS() {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-123");

        String dangerous = "<script>alert(1)</script>";
        CartItem item = new CartItem(dangerous, 1);
        request.setItems(List.of(item));

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }
}
