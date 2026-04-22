package com.apiCodeGenerator.dynamicMaster.v2.generator.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * V2 FieldConfig — Comprehensive Field Descriptor
 * ============================================================
 * Describes a single domain field with full metadata:
 * type info, DB column, validation rules, and API behavior.
 */
@Data
public class FieldConfig {

    // ─────────────────────────────────────────────────────────
    // Identity
    // ─────────────────────────────────────────────────────────

    /** Java field name (camelCase): "categoryCode" */
    private String name;

    /** DB column name (snake_case): "category_code" */
    private String columnName;

    // ─────────────────────────────────────────────────────────
    // Type
    // ─────────────────────────────────────────────────────────

    /**
     * Java type string:
     * String | Long | Integer | Boolean |
     * LocalDate | LocalDateTime | BigDecimal | UUID |
     * Short | Double | Float | Byte | Enum
     */
    private String type;

    /**
     * If type is "Enum", provide the fully qualified enum class name.
     * Example: "com.healthcare.common.enums.GenderEnum"
     */
    private String enumClass;

    // ─────────────────────────────────────────────────────────
    // Column Constraints
    // ─────────────────────────────────────────────────────────

    /** Allows null in DB */
    private boolean nullable = true;

    /** Can be updated after insert */
    private boolean updatable = true;

    /** Can be inserted */
    private boolean insertable = true;

    /** String max length */
    private Integer length;

    /** BigDecimal total digits */
    private Integer precision;

    /** BigDecimal decimal places */
    private Integer scale;

    /** DB column default expression: "0", "'active'", "CURRENT_TIMESTAMP" */
    private String defaultValue;

    /** Unique constraint at column level */
    private boolean unique = false;

    // ─────────────────────────────────────────────────────────
    // API / Query Behavior
    // ─────────────────────────────────────────────────────────

    /** Include in global search (LIKE query) */
    private boolean searchable = false;

    /** Can be filtered by exact match (?fieldName=value) */
    private boolean filterable = false;

    /** Can be sorted by this field */
    private boolean sortable = false;

    /**
     * Exclude this field from the Request DTO.
     * Useful for system-set fields like "sortOrder" set automatically.
     */
    private boolean excludeFromRequest = false;

    /**
     * Exclude this field from the Response DTO.
     * Useful for internal fields.
     */
    private boolean excludeFromResponse = false;

    /**
     * Exclude this field from the Projection (DB query result).
     */
    private boolean excludeFromProjection = false;

    /**
     * Include this field in dropdown response.
     * Defaults to true for searchable fields.
     */
    private boolean includeInDropdown = true;

    // ─────────────────────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────────────────────

    /** Jakarta validation annotations for this field */
    private List<ValidationRule> validations = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Relationship FK Support
    // ─────────────────────────────────────────────────────────

    /**
     * If this field is a FK, specify the referenced entity name.
     * Example: "Department" → resolve via repository
     */
    private String referencedEntity;

    /**
     * UUID field name of the referenced entity for FK resolution.
     * Example: "departmentUuid"
     */
    private String referencedUuidField;

    // ─────────────────────────────────────────────────────────
    // Computed Helpers
    // ─────────────────────────────────────────────────────────

    /** Generates getter method name: name → getName */
    public String getterName() {
        if (name == null || name.isEmpty()) return "get";
        return "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /** Generates setter method name: name → setName */
    public String setterName() {
        if (name == null || name.isEmpty()) return "set";
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /** Returns true if this is a date/time field */
    public boolean isDateField() {
        return "LocalDate".equals(type) || "LocalDateTime".equals(type);
    }

    /** Returns true if this is an Enum field */
    public boolean isEnumField() {
        return "Enum".equals(type) && enumClass != null;
    }
}