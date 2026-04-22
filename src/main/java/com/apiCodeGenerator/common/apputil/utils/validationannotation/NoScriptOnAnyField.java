package com.apiCodeGenerator.common.apputil.utils.validationannotation;

import com.apiCodeGenerator.common.apputil.utils.validationannotation.validator.NoScriptOnAnyFieldValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoScriptOnAnyFieldValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoScriptOnAnyField {
    String message() default "One or more fields contain invalid script content.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
