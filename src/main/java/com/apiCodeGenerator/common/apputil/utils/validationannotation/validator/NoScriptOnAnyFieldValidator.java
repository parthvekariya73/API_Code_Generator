package com.apiCodeGenerator.common.apputil.utils.validationannotation.validator;

import com.apiCodeGenerator.common.apputil.utils.validationannotation.NoScriptOnAnyField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NoScriptOnAnyFieldValidator implements ConstraintValidator<NoScriptOnAnyField, Object> {

    // Re-use the same pattern from our original validator
    private static final java.util.regex.Pattern SCRIPT_PATTERN = java.util.regex.Pattern.compile(
            "<script.*?>.*?</script>|javascript:|on\\w+\\s*=",
            java.util.regex.Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return true;
        }

        List<String> fieldsWithScripts = new ArrayList<>();
        Class<?> clazz = object.getClass();

        // Use reflection to get all declared fields of the class
        for (Field field : clazz.getDeclaredFields()) {
            // We only care about String fields
            if (field.getType() == String.class) {
                try {
                    // Make private fields accessible
                    field.setAccessible(true);
                    String value = (String) field.get(object);

                    if (value != null && SCRIPT_PATTERN.matcher(value).find()) {
                        fieldsWithScripts.add(field.getName());
                    }
                } catch (IllegalAccessException e) {
                    // Log this error, but don't fail validation
                    System.err.println("Error accessing field: " + field.getName());
                }
            }
        }

        // If no fields contain scripts, it's valid
        if (fieldsWithScripts.isEmpty()) {
            return true;
        }

        // If we found invalid fields, build a specific error message
        context.disableDefaultConstraintViolation(); // Disable the default class-level message
        for (String fieldName : fieldsWithScripts) {
            context.buildConstraintViolationWithTemplate("Field contains invalid script content.")
                    .addPropertyNode(fieldName)
                    .addConstraintViolation();
        }

        return false;
    }
}