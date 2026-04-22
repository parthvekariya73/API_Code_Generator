package com.apiCodeGenerator.common.apputil.utils.validationannotation.validator;

import com.apiCodeGenerator.common.apputil.utils.validationannotation.Unique;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class UniqueValidator implements ConstraintValidator<Unique, Object> {

    private final ApplicationContext applicationContext;

    private Class<?> repositoryClass;
    private String fieldName;
    private String methodName;

    @Override
    public void initialize(Unique annotation) {
        this.repositoryClass = annotation.repository();
        this.fieldName = annotation.field();
        this.methodName = annotation.method();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // nullity handled by @NotNull if needed
        }

        // Get the repository bean from Spring context
        Object repository = applicationContext.getBean(repositoryClass);

        // Determine method name
        String methodToCall = methodName;
        if (!StringUtils.hasText(methodToCall)) {
            // Build default method name: "existsBy" + capitalized field name
            String field = StringUtils.hasText(fieldName) ? fieldName : getFieldName(context);
            methodToCall = "existsBy" + capitalize(field);
        }

        try {
            Method method = repository.getClass().getMethod(methodToCall, String.class);
            boolean exists = (boolean) method.invoke(repository, value);
            return !exists;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Repository " + repositoryClass.getSimpleName() +
                    " does not have method " + methodToCall + "(String)", e);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking uniqueness check", e);
        }
    }

    private String getFieldName(ConstraintValidatorContext context) {
        // Extract the field name from the constraint descriptor (a bit tricky)
        // In practice, we can rely on the annotation's field attribute being set,
        // or we could use reflection to get the annotated field name.
        // For simplicity, we require the field name to be provided explicitly in the annotation.
        // If not, we throw an exception.
        throw new IllegalArgumentException("Field name must be specified in @Unique when not used on a field with a simple name");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}


