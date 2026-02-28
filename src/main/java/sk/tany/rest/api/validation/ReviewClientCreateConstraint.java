package sk.tany.rest.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ReviewClientCreateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReviewClientCreateConstraint {
    String message() default "Invalid ReviewClientCreateRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
