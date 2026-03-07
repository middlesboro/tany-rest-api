package sk.tany.rest.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class MongoIdValidator implements ConstraintValidator<MongoId, String> {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return ID_PATTERN.matcher(value).matches();
    }
}
