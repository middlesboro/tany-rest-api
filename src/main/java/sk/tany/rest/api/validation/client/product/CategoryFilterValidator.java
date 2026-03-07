package sk.tany.rest.api.validation.client.product;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.jsoup.parser.Parser;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.dto.request.FilterParameterRequest;
import sk.tany.rest.api.service.HtmlSanitizerService;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class CategoryFilterValidator implements ConstraintValidator<CategoryFilterConstraint, CategoryFilterRequest> {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");
    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public boolean isValid(CategoryFilterRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;

        if (request.getFilterParameters() != null) {
            for (int i = 0; i < request.getFilterParameters().size(); i++) {
                FilterParameterRequest param = request.getFilterParameters().get(i);
                if (param.getId() != null) {
                    if (!ID_PATTERN.matcher(param.getId()).matches()) {
                        addViolation(context, "filterParameters[" + i + "].id", "Invalid ID format");
                        valid = false;
                    } else if (isDangerous(param.getId())) {
                        addViolation(context, "filterParameters[" + i + "].id", "contains dangerous content");
                        valid = false;
                    }
                }

                if (param.getFilterParameterValueIds() != null) {
                    for (int j = 0; j < param.getFilterParameterValueIds().size(); j++) {
                        String valueId = param.getFilterParameterValueIds().get(j);
                        if (valueId != null) {
                            if (!ID_PATTERN.matcher(valueId).matches()) {
                                addViolation(context, "filterParameters[" + i + "].filterParameterValueIds[" + j + "]", "Invalid ID format");
                                valid = false;
                            } else if (isDangerous(valueId)) {
                                addViolation(context, "filterParameters[" + i + "].filterParameterValueIds[" + j + "]", "contains dangerous content");
                                valid = false;
                            }
                        }
                    }
                }
            }
        }

        if (request.getSort() != null && isDangerous(request.getSort().name())) {
            addViolation(context, "sort", "contains dangerous content");
            valid = false;
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
        }

        return valid;
    }

    private boolean isDangerous(String value) {
        if (value == null || value.isBlank()) return false;
        String sanitized = htmlSanitizerService.sanitize(value);
        return !value.equals(Parser.unescapeEntities(sanitized, false));
    }

    private void addViolation(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(property)
               .addConstraintViolation();
    }
}
