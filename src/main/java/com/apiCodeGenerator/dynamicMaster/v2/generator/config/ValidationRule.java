package com.apiCodeGenerator.dynamicMaster.v2.generator.config;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a single Jakarta validation annotation on a field.
 *
 * Example JSON:
 * {
 *   "annotation": "Size",
 *   "attributes": { "min": 2, "max": 50 },
 *   "message": "Must be between 2 and 50 characters"
 * }
 */
@Data
public class ValidationRule {

    /** Annotation simple name: "NotBlank", "Size", "Min", "Max", "Pattern", "Email" */
    private String annotation;

    /**
     * Annotation attribute key-value pairs.
     * String values will be quoted; numeric/boolean values will not.
     */
    private Map<String, Object> attributes = new HashMap<>();

    /** Validation failure message */
    private String message;

    /** Safe getter — never returns null */
    public Map<String, Object> safeAttributes() {
        return attributes != null ? attributes : new HashMap<>();
    }

    /** Returns true if this validation has attributes */
    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }
}