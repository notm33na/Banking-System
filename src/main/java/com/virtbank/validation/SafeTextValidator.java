package com.virtbank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SafeTextValidator implements ConstraintValidator<SafeText, String> {

    // Case-insensitive detection of common SQL keywords used in injection attacks
    private static final Pattern SQL_PATTERN = Pattern.compile(
            "\\b(DROP|SELECT|INSERT|DELETE|UPDATE|ALTER|EXEC|EXECUTE|UNION|CREATE|TRUNCATE)\\b|--|;\\s*(DROP|SELECT)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true;
        return !SQL_PATTERN.matcher(value).find();
    }
}
