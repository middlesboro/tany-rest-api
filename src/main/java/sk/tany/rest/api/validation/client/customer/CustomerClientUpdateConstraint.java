package sk.tany.rest.api.validation.client.customer;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CustomerClientUpdateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomerClientUpdateConstraint {
    String message() default "Invalid CustomerClientUpdateRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
