package tn.esprit.cv_generator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRanged> {

    @Override
    public boolean isValid(DateRanged value, ConstraintValidatorContext context) {
        if (value.getStartDate() == null || value.getEndDate() == null) {
            return true; // null fields are caught by @NotNull / field-level constraints
        }
        return !value.getEndDate().isBefore(value.getStartDate());
    }
}
