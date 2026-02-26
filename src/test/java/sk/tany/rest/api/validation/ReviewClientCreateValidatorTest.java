package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewClientCreateValidatorTest {

    @InjectMocks
    private ReviewClientCreateValidator validator;

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
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        request.setProductId("123");
        request.setText("Great product");
        request.setTitle("Review");
        request.setEmail("test@example.com");

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void isValid_ShouldReturnFalse_WhenProductIdContainsXSS() {
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        String dangerous = "<script>";
        request.setProductId(dangerous);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
        verify(builder).addPropertyNode("productId");
    }

    @Test
    void isValid_ShouldReturnFalse_WhenTextContainsXSS() {
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        String dangerous = "<script>";
        request.setText(dangerous);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
        verify(builder).addPropertyNode("text");
    }

    @Test
    void isValid_ShouldReturnFalse_WhenTitleContainsXSS() {
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        String dangerous = "<script>";
        request.setTitle(dangerous);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
        verify(builder).addPropertyNode("title");
    }

    @Test
    void isValid_ShouldReturnFalse_WhenEmailContainsXSS() {
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        String dangerous = "<script>";
        request.setEmail(dangerous);

        when(htmlSanitizerService.sanitize(dangerous)).thenReturn("");

        boolean result = validator.isValid(request, context);

        assertFalse(result);
        verify(builder).addPropertyNode("email");
    }
}
