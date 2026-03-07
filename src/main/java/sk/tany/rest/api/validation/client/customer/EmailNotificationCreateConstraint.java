package sk.tany.rest.api.validation.client.customer;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EmailNotificationCreateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailNotificationCreateConstraint {
    String message() default "Invalid EmailNotificationCreateRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
