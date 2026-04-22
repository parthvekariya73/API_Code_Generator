package com.apiCodeGenerator.dynamicMaster.v2.generator.core;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 NamingUtil — Centralized Naming Conventions
 * ============================================================
 * All name transformations live here.
 * No more scattered charAt(0) / substring(1) tricks
 * duplicated across every generator class.
 *
 * All methods are static — import statically for clean usage.
 */
public final class NamingUtil {

    private NamingUtil() { /* utility class */ }

    // ─────────────────────────────────────────────────────────
    // Entity / Class Names
    // ─────────────────────────────────────────────────────────

    /**
     * Convert snake_case module name to PascalCase entity name.
     *
     * "product_category" → "ProductCategory"
     * "drug_master"      → "DrugMaster"
     */
    public static String toEntityName(ModuleConfig config) {
        return toPascalCase(config.getModuleName());
    }

    /**
     * Convert any snake_case or camelCase string to PascalCase.
     */
    public static String toPascalCase(String input) {
        return Arrays.stream(input.split("[_\\s]+"))
                .filter(p -> !p.isEmpty())
                .map(NamingUtil::capitalize)
                .collect(Collectors.joining());
    }

    /**
     * Convert PascalCase or camelCase to snake_case kebab URL path.
     * "ProductCategory" → "product-category"
     * "drugMaster"      → "drug-master"
     */
    public static String toUrlPath(String moduleName) {
        return moduleName.replace("_", "-");
    }

    /**
     * Convert module name to kebab-case REST path segment.
     * "product_category" → "product-category"
     */
    public static String toRestPath(ModuleConfig config) {
        return toUrlPath(config.getModuleName());
    }

    // ─────────────────────────────────────────────────────────
    // Field Accessors
    // ─────────────────────────────────────────────────────────

    /**
     * Generate getter method name.
     * "categoryName" → "getCategoryName"
     */
    public static String getter(String fieldName) {
        return "get" + capitalize(fieldName);
    }

    /**
     * Generate setter method name.
     * "categoryName" → "setCategoryName"
     */
    public static String setter(String fieldName) {
        return "set" + capitalize(fieldName);
    }

    /**
     * Capitalize first letter, keep rest as-is.
     * "categoryName" → "CategoryName"
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Lowercase first letter.
     * "ProductCategory" → "productCategory"
     */
    public static String decapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    // ─────────────────────────────────────────────────────────
    // Type Helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Map type string to Java type.
     * Handles all common types cleanly via switch expression.
     */
    public static String toJavaType(String type) {
        if (type == null) return "String";
        return switch (type.toLowerCase()) {
            case "string"        -> "String";
            case "long"          -> "Long";
            case "integer", "int" -> "Integer";
            case "short"         -> "Short";
            case "byte"          -> "Byte";
            case "boolean"       -> "Boolean";
            case "float"         -> "Float";
            case "double"        -> "Double";
            case "bigdecimal"    -> "BigDecimal";
            case "localdate"     -> "LocalDate";
            case "localdatetime" -> "LocalDateTime";
            case "uuid"          -> "UUID";
            default              -> type; // pass-through for enums, custom types
        };
    }

    /**
     * Map Java type to DB column type (for documentation/DDL hints).
     */
    public static String toDbType(String javaType) {
        if (javaType == null) return "VARCHAR";
        return switch (javaType.toLowerCase()) {
            case "string"        -> "VARCHAR";
            case "long"          -> "BIGINT";
            case "integer", "int" -> "INTEGER";
            case "short"         -> "SMALLINT";
            case "boolean"       -> "BOOLEAN";
            case "localdate"     -> "DATE";
            case "localdatetime" -> "TIMESTAMP";
            case "bigdecimal"    -> "DECIMAL";
            case "uuid"          -> "UUID";
            case "double"        -> "DOUBLE PRECISION";
            case "float"         -> "REAL";
            default              -> "VARCHAR";
        };
    }

    // ─────────────────────────────────────────────────────────
    // Package / Path
    // ─────────────────────────────────────────────────────────

    /**
     * Convert package name to directory path.
     * "com.healthcare.product" → "com/healthcare/product"
     */
    public static String packageToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    // ─────────────────────────────────────────────────────────
    // Annotation Helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Format a value for use in an annotation attribute.
     * Strings → quoted, Numbers/Booleans → raw, Enum → NAME
     */
    public static String formatAnnotationValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + s + "\"";
        if (value instanceof Enum<?> e) return e.name();
        return value.toString();
    }
}