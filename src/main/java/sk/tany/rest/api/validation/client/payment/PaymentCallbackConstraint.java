package sk.tany.rest.api.validation.client.payment;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PaymentCallbackValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentCallbackConstraint {
    String message() default "Invalid PaymentCallbackRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
