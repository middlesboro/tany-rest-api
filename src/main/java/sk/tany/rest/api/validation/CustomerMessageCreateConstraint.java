package sk.tany.rest.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CustomerMessageCreateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomerMessageCreateConstraint {

    String message() default "Invalid customer message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
