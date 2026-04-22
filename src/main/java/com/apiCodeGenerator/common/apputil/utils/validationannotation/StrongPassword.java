package com.apiCodeGenerator.common.apputil.utils.validationannotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Size(
        min = 8, max = 100,
        message = "{register.password.size}"
)
@Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#^()\\-])[A-Za-z\\d@$!%*?&._#^()\\-]{8,}$",
        message = "{register.password.pattern}"
)
public @interface StrongPassword {

    String message() default "{register.password.pattern}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}