package com.apiCodeGenerator.dynamicMaster.v2.generator.config;

import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a unique constraint across one or more fields.
 * Drives generation of:
 *  - existsBy...() and existsBy...AndNotId() repository queries
 *  - Duplicate-check logic in ServiceImpl
 */
@Data
public class UniqueConstraint {

    /** DB constraint name: "uq_category_code" */
    private String name;

    /**
     * Field names involved in the constraint.
     * Multi-field → composite uniqueness check.
     */
    private List<String> fields;

    /** Error message when violated: "Category code already exists" */
    private String message;

    /**
     * Builds the method name suffix for exists queries.
     * ["categoryCode"] → "ByCategoryCode"
     * ["firstName", "lastName"] → "ByFirstNameAndLastName"
     */
    public String methodSuffix() {
        return fields.stream()
                .map(f -> Character.toUpperCase(f.charAt(0)) + f.substring(1))
                .collect(Collectors.joining("And"));
    }

    /**
     * Builds the existsBy... method name.
     * Example: "existsByCategoryCode"
     */
    public String existsMethodName() {
        return "existsBy" + methodSuffix();
    }

    /**
     * Builds the existsBy...AndNotId method name for update checks.
     * Example: "existsByCategoryCodeAndNotId"
     */
    public String existsNotIdMethodName() {
        return "existsBy" + methodSuffix() + "AndNotId";
    }
}