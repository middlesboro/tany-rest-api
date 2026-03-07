package sk.tany.rest.api.validation.client.cart;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CartClientAddItemValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CartClientAddItemConstraint {
    String message() default "Invalid CartClientAddItemRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
