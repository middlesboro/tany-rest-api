package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    // More permissive regex to support modern email formats (including '+' alias and long TLDs).
    // Matches: [local part]@[domain].[tld]
    // Local part allows: alphanumeric, dots, hyphens, pluses, underscores.
    // Domain allows: alphanumeric, hyphens.
    // TLD allows: alphanumeric, at least 2 chars.
    private static final String EMAIL_PATTERN = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true;
        }
        return PATTERN.matcher(email).matches();
    }
}
