package com.apiCodeGenerator.common.apputil.utils.validationannotation.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DynamicFieldValidator implements ConstraintValidator<ValidField, String> {

    private boolean required;
    private int min;
    private int max;
    private String pattern;
    private String fieldName;

    @Override
    public void initialize(ValidField annotation) {
        this.required = annotation.required();
        this.min = annotation.min();
        this.max = annotation.max();
        this.pattern = annotation.pattern();
        this.fieldName = annotation.fieldName();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        // 1. Required check
        if (required && (value == null || value.trim().isEmpty())) {
            context.buildConstraintViolationWithTemplate(fieldName + " is required")
                    .addConstraintViolation();
            return false;
        }

        // If empty and not required → skip further checks
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // 2. Size check
        if (min != -1 && max != -1 && (value.length() < min || value.length() > max)) {
            context.buildConstraintViolationWithTemplate(
                            fieldName + " must be between " + min + " and " + max + " characters")
                    .addConstraintViolation();
            return false;
        }

        // 3. Pattern check
        if (!pattern.isEmpty() && !value.matches(pattern)) {
            context.buildConstraintViolationWithTemplate(
                            fieldName + " has invalid format")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}