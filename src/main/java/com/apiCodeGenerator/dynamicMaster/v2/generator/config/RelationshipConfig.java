package com.apiCodeGenerator.dynamicMaster.v2.generator.config;

import lombok.Data;

/**
 * Defines a JPA relationship from this entity to another.
 *
 * Supported types: ManyToOne | OneToMany | OneToOne | ManyToMany
 */
@Data
public class RelationshipConfig {

    /** Field name for this relationship: "department", "items" */
    private String name;

    /** Relationship type: "ManyToOne" | "OneToMany" | "OneToOne" | "ManyToMany" */
    private String type;

    /** Target entity simple class name: "Department", "OrderItem" */
    private String targetEntity;

    /** Target entity fully qualified package (if not same package) */
    private String targetPackage;

    /** Join column name for owning side: "department_id" */
    private String joinColumn;

    /** mappedBy value for inverse side: "department" */
    private String mappedBy;

    /** Use LAZY fetch (recommended for collections) */
    private boolean lazy = true;

    /** Apply cascade type: "ALL" | "PERSIST" | "MERGE" | "REMOVE" | "REFRESH" */
    private String cascade;

    /** Whether to include this relationship field in the Response DTO */
    private boolean includeInResponse = false;
}