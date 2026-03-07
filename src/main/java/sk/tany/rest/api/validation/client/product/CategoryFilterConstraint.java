package sk.tany.rest.api.validation.client.product;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CategoryFilterValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CategoryFilterConstraint {
    String message() default "Invalid CategoryFilterRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
