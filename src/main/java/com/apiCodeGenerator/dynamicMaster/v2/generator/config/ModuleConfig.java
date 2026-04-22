package com.apiCodeGenerator.dynamicMaster.v2.generator.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * V2 ModuleConfig — Single Source of Truth for Code Generation
 * ============================================================
 * Defines every aspect of a module: entity, API, validation,
 * relationships, caching, export, and more.
 *
 * Usage: Populate this from a JSON config file and pass to
 *        CodeGeneratorEngine to produce all Spring Boot classes.
 */
@Data
public class ModuleConfig {

    // ─────────────────────────────────────────────────────────
    // Core Module Identity
    // ─────────────────────────────────────────────────────────

    /** Module name in snake_case: "product_category", "drug_master" */
    private String moduleName;

    /** Root Java package: "com.healthcare.product.category" */
    private String packageName;

    /** Database table name: "mst_product_category" */
    private String tableName;

    /** Database schema: "master", "inventory", "public" */
    private String schemaName;

    // ─────────────────────────────────────────────────────────
    // Primary Key Config
    // ─────────────────────────────────────────────────────────

    /** PK field name: "categoryId" */
    private String primaryKey;

    /** PK Java type: "Long" | "Integer" | "UUID" */
    private String primaryKeyType = "Long";

    /** Optional DB sequence name for PK generation */
    private String primaryKeySequence;

    // ─────────────────────────────────────────────────────────
    // Standard System Fields
    // ─────────────────────────────────────────────────────────

    /** UUID field name: "categoryUuid" */
    private String uuidField;

    /** Soft delete column: "status" */
    private String softDeleteField = "status";

    /**
     * Enable audit fields: createdAt, updatedAt, createdBy,
     * updatedBy, deletedAt, deletedBy, auditTrackerId
     */
    private boolean auditFields = true;

    // ─────────────────────────────────────────────────────────
    // Business Fields
    // ─────────────────────────────────────────────────────────

    /** All domain fields for this module */
    private List<FieldConfig> fields = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Constraints & Validation
    // ─────────────────────────────────────────────────────────

    /** Unique constraint definitions (generates exists-check queries) */
    private List<UniqueConstraint> uniqueConstraints = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────

    /** JPA relationships: @ManyToOne, @OneToMany, etc. */
    private List<RelationshipConfig> relationships = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // API Behavior
    // ─────────────────────────────────────────────────────────

    /**
     * Default sort field for getAll queries.
     * If null, falls back to "createdAt"
     */
    private String defaultSortField = "createdAt";

    /** Default sort direction: "ASC" | "DESC" */
    private String defaultSortDirection = "DESC";

    /** Default page size for pagination */
    private int defaultPageSize = 10;

    /** Enable filter-by-status endpoint (GET /filter?status=active) */
    private boolean enableStatusFilter = true;

    /** Enable bulk soft-delete endpoint (DELETE /bulk) */
    private boolean enableBulkDelete = false;

    /** Enable export endpoint (GET /export?format=excel|csv) */
    private boolean enableExport = false;

    /** Enable count endpoint (GET /count) */
    private boolean enableCount = true;

    /** Enable dropdown endpoint (GET /dropdown) for select options */
    private boolean enableDropdown = false;

    // ─────────────────────────────────────────────────────────
    // Caching
    // ─────────────────────────────────────────────────────────

    /** Enable Spring @Cacheable on getByUuid and getAll */
    private boolean enableCache = false;

    /** Cache name to use: defaults to moduleName */
    private String cacheName;

    // ─────────────────────────────────────────────────────────
    // Custom Endpoints
    // ─────────────────────────────────────────────────────────

    /** Additional non-standard API endpoints */
    private List<ApiEndpointConfig> customEndpoints = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Error Code Overrides
    // ─────────────────────────────────────────────────────────

    /**
     * Custom error code for "not found". Defaults to RESOURCE_NOT_FOUND.
     * Example: "CATEGORY_NOT_FOUND"
     */
    private String notFoundErrorCode;

    /**
     * Custom error code for duplicates. Defaults to DUPLICATE_RESOURCE.
     * Example: "CATEGORY_ALREADY_EXISTS"
     */
    private String duplicateErrorCode;

    // ─────────────────────────────────────────────────────────
    // Dropdown / Lookup Config
    // ─────────────────────────────────────────────────────────

    /**
     * Field to use as dropdown label.
     * Example: "categoryName" → {value: uuid, label: categoryName}
     */
    private String dropdownLabelField;

    // ─────────────────────────────────────────────────────────
    // Computed / Derived Helpers
    // ─────────────────────────────────────────────────────────

    /** Resolved cache name (moduleName if cacheName is null) */
    public String resolvedCacheName() {
        return cacheName != null ? cacheName : moduleName;
    }

    /** Resolved not-found error code */
    public String resolvedNotFoundCode() {
        return notFoundErrorCode != null ? notFoundErrorCode : "ErrorCode.RESOURCE_NOT_FOUND";
    }

    /** Resolved duplicate error code */
    public String resolvedDuplicateCode() {
        return duplicateErrorCode != null ? duplicateErrorCode : "ErrorCode.DUPLICATE_RESOURCE";
    }
}