package com.apiCodeGenerator.common.apputil.utils.validationannotation;

import com.apiCodeGenerator.common.apputil.utils.validationannotation.validator.UniqueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidator.class)
@Documented
public @interface Unique {

    String message() default "Value already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * The repository class (e.g., MahRepository.class) that contains the existence check method.
     */
    Class<?> repository();

    /**
     * The name of the field in the entity (optional – defaults to the annotated field name).
     * Used to construct the default method name: "existsBy" + capitalized field name.
     */
    String field() default "";

    /**
     * Explicit name of the repository method to call (optional).
     * If not provided, the validator constructs "existsBy" + capitalized field name.
     * The method must accept a single String argument and return boolean.
     */
    String method() default "";
}