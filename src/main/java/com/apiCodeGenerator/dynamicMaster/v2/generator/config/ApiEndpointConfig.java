package com.apiCodeGenerator.dynamicMaster.v2.generator.config;

import lombok.Data;
import java.util.List;

/**
 * Defines a custom API endpoint to be generated beyond CRUD defaults.
 *
 * Example: a "findByCode" endpoint, or "getActiveOnly".
 */
@Data
public class ApiEndpointConfig {

    /** HTTP method: "GET" | "POST" | "PUT" | "DELETE" | "PATCH" */
    private String method;

    /** URL path segment: "/by-code/{code}" */
    private String path;

    /** Service method name to invoke: "findByCode" */
    private String serviceMethod;

    /** Short description for Swagger @Operation */
    private String description;

    /** Whether this endpoint returns a Page */
    private boolean paginated = false;

    /** Required Spring Security permissions/roles */
    private List<String> requiredPermissions;
}