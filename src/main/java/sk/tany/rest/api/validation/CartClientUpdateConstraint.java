package sk.tany.rest.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CartClientUpdateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CartClientUpdateConstraint {
    String message() default "Invalid CartClientUpdateRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
