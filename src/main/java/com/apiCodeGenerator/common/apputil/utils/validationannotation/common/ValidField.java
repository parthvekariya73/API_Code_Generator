package com.apiCodeGenerator.common.apputil.utils.validationannotation.common;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DynamicFieldValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidField {

    boolean required() default false;

    int min() default -1;
    int max() default -1;

    String pattern() default "";

    String fieldName() default "Field"; // for dynamic messages

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String message() default "Invalid field";
}