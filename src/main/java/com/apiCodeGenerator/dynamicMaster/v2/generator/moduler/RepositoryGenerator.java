package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.UniqueConstraint;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.ImportRegistry;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 RepositoryGenerator — JPA Repository Interface
 * ============================================================
 * Generates Spring Data JPA repository with:
 *  - findByUuid / findIdByUuid
 *  - existsBy... / existsBy...AndNotId (per unique constraint)
 *  - softDeleteById
 *  - findAllWithFilters — searchable + filterable fields
 *  - findProjectionByUuid — native SQL with JOINs
 *  - countActive — total non-deleted records
 *  - findAllForDropdown — lightweight label/value pairs
 *
 * All queries use native SQL for performance (no JPQL overhead).
 */
@Slf4j
@Component("RepositoryGeneratorV2")
public class RepositoryGenerator {

    public String generate(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating Repository: {}Repository", entityName);

        ImportRegistry imports = buildImports(config, entityName);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".repository;")
                .blank()
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                .line("/**")
                .line(" * Repository for " + entityName + ".")
                .line(" * All paginated queries use native SQL for optimal DB performance.")
                .line(" */")
                .line("@Repository")
                .line("public interface " + entityName + "Repository extends JpaRepository<" + entityName + ", Long> {")
                .blank()
                // ── Standard Finders ──────────────────────────────
                .indent(t -> generateStandardFinders(t, config, entityName))
                // ── Unique Constraint Checks ──────────────────────
                .indent(t -> generateUniqueConstraintChecks(t, config, entityName))
                // ── Soft Delete ───────────────────────────────────
                .indent(t -> generateSoftDelete(t, config, entityName))
                // ── Paginated Listing ─────────────────────────────
                .indent(t -> generateFindAllWithFilters(t, config, entityName))
                // ── Single Record Projection ──────────────────────
                .indent(t -> generateFindProjectionByUuid(t, config, entityName))
                // ── Count Active ──────────────────────────────────
                .when(config.isEnableCount(), t ->
                        t.indent(tt -> generateCountActive(tt, config, entityName)))
                // ── Dropdown Query ────────────────────────────────
                .when(config.isEnableDropdown() && config.getDropdownLabelField() != null, t ->
                        t.indent(tt -> generateDropdownQuery(tt, config, entityName)))
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // Imports
    // ─────────────────────────────────────────────────────────

    private ImportRegistry buildImports(ModuleConfig config, String entityName) {
        String pkg = config.getPackageName();
        return new ImportRegistry()
                .add(pkg + ".entity." + entityName)
                .add(pkg + ".dto.projection." + entityName + "Projection")
                .addAll(
                        "org.springframework.data.domain.Page",
                        "org.springframework.data.domain.Pageable",
                        "org.springframework.data.jpa.repository.JpaRepository",
                        "org.springframework.data.jpa.repository.Modifying",
                        "org.springframework.data.jpa.repository.Query",
                        "org.springframework.data.repository.query.Param",
                        "org.springframework.stereotype.Repository",
                        "java.util.List",
                        "java.util.Optional",
                        "java.util.UUID"
                );
    }

    // ─────────────────────────────────────────────────────────
    // Standard Finders
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateStandardFinders(CodeTemplate t, ModuleConfig config, String entityName) {
        String uuid = config.getUuidField();
        String pk = config.getPrimaryKey();

        return t
                .line("// ── Standard Finders ─────────────────────────────────────")
                .blank()
                .line("/** Find active entity by UUID */")
                .line("@Query(\"SELECT e FROM " + entityName + " e WHERE e." + uuid + " = :uuid AND e.status != 9\")")
                .line("Optional<" + entityName + "> findByUuid(@Param(\"uuid\") UUID uuid);")
                .blank()
                .line("/** Fetch only the PK for a given UUID — avoids full entity load */")
                .line("@Query(\"SELECT e." + pk + " FROM " + entityName + " e WHERE e." + uuid + " = :uuid AND e.status != 9\")")
                .line("Optional<Long> findIdByUuid(@Param(\"uuid\") UUID uuid);");
    }

    // ─────────────────────────────────────────────────────────
    // Unique Constraint Checks
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUniqueConstraintChecks(CodeTemplate t, ModuleConfig config, String entityName) {
        if (config.getUniqueConstraints().isEmpty()) return t;

        t.blank().line("// ── Unique Constraint Checks ─────────────────────────────");

        for (UniqueConstraint uc : config.getUniqueConstraints()) {
            generateExistsMethods(t, uc, config, entityName);
        }
        return t;
    }

    private void generateExistsMethods(CodeTemplate t, UniqueConstraint uc,
                                       ModuleConfig config, String entityName) {
        // Build conditions based on field types
        String conditions = uc.getFields().stream()
                .map(f -> {
                    String type = getFieldType(config, f);
                    if ("String".equalsIgnoreCase(type)) {
                        return "LOWER(e." + f + ") = LOWER(:" + f + ")";
                    } else if ("UUID".equalsIgnoreCase(type)) {
                        // Cast UUID to text for safer comparison in some DBs/JPQL versions if needed, 
                        // but usually e.f = :f is fine for UUID.
                        return "e." + f + " = :" + f;
                    } else {
                        return "e." + f + " = :" + f;
                    }
                })
                .collect(Collectors.joining(" AND "));

        // Method parameters with correct Java types
        String params = uc.getFields().stream()
                .map(f -> "@Param(\"" + f + "\") " + NamingUtil.toJavaType(getFieldType(config, f)) + " " + f)
                .collect(Collectors.joining(", "));

        // ── Create-time exists check ──────────────────────────
        t.blank()
                .line("/** Check for duplicate " + uc.getName() + " on create */")
                .line("@Query(\"\"\"")
                .line("        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END")
                .line("        FROM " + entityName + " e")
                .line("        WHERE " + conditions + " AND e.status != 9\"\"\")")
                .line("boolean " + uc.existsMethodName() + "(" + params + ");")
                .blank()
                .line("/** Check for duplicate " + uc.getName() + " on update (exclude current record) */")
                .line("@Query(\"\"\"")
                .line("        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END")
                .line("        FROM " + entityName + " e")
                .line("        WHERE " + conditions + " AND e.status != 9")
                .line("          AND e." + config.getPrimaryKey() + " != :excludeId\"\"\")")
                .line("boolean " + uc.existsNotIdMethodName() + "(@Param(\"excludeId\") Long excludeId, " + params + ");");
    }

    // ─────────────────────────────────────────────────────────
    // Soft Delete
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateSoftDelete(CodeTemplate t, ModuleConfig config, String entityName) {
        return t.blank()
                .line("// ── Soft Delete ───────────────────────────────────────────")
                .blank()
                .line("@Modifying")
                .line("@Query(\"\"\"")
                .line("        UPDATE " + entityName + " e")
                .line("        SET e.status = 9, e.deletedAt = CURRENT_TIMESTAMP, e.deletedBy = :deletedBy")
                .line("        WHERE e." + config.getPrimaryKey() + " = :id\"\"\")")
                .line("void softDeleteById(@Param(\"id\") Long id, @Param(\"deletedBy\") Long deletedBy);");
    }

    // ─────────────────────────────────────────────────────────
    // Paginated List Query (Native SQL)
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateFindAllWithFilters(CodeTemplate t, ModuleConfig config, String entityName) {
        String selectBlock   = buildSelectBlock(config);
        String searchBlock   = buildSearchBlock(config);
        String filterBlock   = buildFilterBlock(config);
        String fromBlock     = buildFromBlock(config);
        String countQuery    = buildCountQuery(config);

        return t.blank()
                .line("// ── Paginated Listing with Search + Filter ───────────────")
                .blank()
                .line("@Query(value = \"\"\"")
                .raw("        SELECT")
                .raw(selectBlock)
                .raw(fromBlock)
                .raw("        WHERE e.status != 9")
                .raw(searchBlock)
                .raw(filterBlock)
                .raw("        ORDER BY e." + config.getDefaultSortField() + " " + config.getDefaultSortDirection())
                .line("        \"\"\",")
                .line("        countQuery = \"\"\"")
                .raw(countQuery)
                .line("        \"\"\",")
                .line("        nativeQuery = true)")
                .line("Page<" + entityName + "Projection> findAllWithFilters(")
                .line("        @Param(\"search\") String search,")
                .append(buildFilterParams(config))
                .line("        Pageable pageable);");
    }

    private String buildSelectBlock(ModuleConfig config) {
        List<String> cols = new java.util.ArrayList<>();
        cols.add("            e." + config.getPrimaryKey() + " as " + config.getPrimaryKey());
        cols.add("            e." + config.getUuidField() + " as " + config.getUuidField());

        config.getFields().stream()
                .filter(f -> !f.isExcludeFromProjection())
                .forEach(f -> cols.add("            e." + f.getColumnName() + " as " + f.getName()));

        cols.add("""
                              CASE
                                  WHEN e.status = 1 THEN 'active'
                                  WHEN e.status = 0 THEN 'inactive'
                                  WHEN e.status = 9 THEN 'deleted'
                                  ELSE 'unknown'
                              END as status""");
        cols.add("            e.created_at as createdAt");
        cols.add("            e.updated_at as updatedAt");
        cols.add("            mu1.full_name as createdBy");
        cols.add("            mu2.full_name as updatedBy");

        return String.join(",\n", cols);
    }

    private String buildFromBlock(ModuleConfig config) {
        return "        FROM " + config.getSchemaName() + "." + config.getTableName() + " e\n" +
                "        LEFT JOIN master.mst_users mu1 ON e.created_by = mu1.user_id\n" +
                "        LEFT JOIN master.mst_users mu2 ON e.updated_by = mu2.user_id";
    }

    private String buildSearchBlock(ModuleConfig config) {
        List<FieldConfig> searchable = config.getFields().stream()
                .filter(FieldConfig::isSearchable)
                .toList();

        if (searchable.isEmpty()) return "";

        String conditions = searchable.stream()
                .map(f -> "  LOWER(e." + f.getColumnName() + ") LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))")
                .collect(Collectors.joining("\n            OR "));

        return "\n          AND (CAST(:search AS text) IS NULL OR \n" + conditions + ")";
    }

    private String buildFilterBlock(ModuleConfig config) {
        return config.getFields().stream()
                .filter(FieldConfig::isFilterable)
                .map(f -> "\n          AND (CAST(:" + f.getName() + " AS text) IS NULL OR e." + f.getColumnName() + " = CAST(:" + f.getName() + " AS text))")
                .collect(Collectors.joining());
    }

    private String buildCountQuery(ModuleConfig config) {
        return "        SELECT COUNT(1)\n" +
                "        FROM " + config.getSchemaName() + "." + config.getTableName() + " e\n" +
                "        WHERE e.status != 9";
    }

    private CodeTemplate buildFilterParams(ModuleConfig config) {
        CodeTemplate t = CodeTemplate.of();
        config.getFields().stream()
                .filter(FieldConfig::isFilterable)
                .forEach(f -> t.line("        @Param(\"" + f.getName() + "\") String " + f.getName() + ","));
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // Single Record Projection by UUID
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateFindProjectionByUuid(CodeTemplate t, ModuleConfig config, String entityName) {
        String selectBlock = buildSelectBlock(config);
        String fromBlock   = buildFromBlock(config);

        return t.blank()
                .line("// ── Single Record by UUID ─────────────────────────────────")
                .blank()
                .line("@Query(value = \"\"\"")
                .raw("        SELECT")
                .raw(selectBlock)
                .raw(fromBlock)
                .raw("        WHERE e." + config.getUuidField() + " = CAST(:uuid AS uuid) AND e.status != 9")
                .line("        \"\"\", nativeQuery = true)")
                .line("Optional<" + entityName + "Projection> findProjectionByUuid(@Param(\"uuid\") UUID uuid);");
    }

    // ─────────────────────────────────────────────────────────
    // Count Active
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateCountActive(CodeTemplate t, ModuleConfig config, String entityName) {
        return t.blank()
                .line("// ── Count Active Records ─────────────────────────────────")
                .blank()
                .line("@Query(value = \"SELECT COUNT(1) FROM " + config.getSchemaName() + "." +
                        config.getTableName() + " WHERE status != 9\", nativeQuery = true)")
                .line("long countActive();");
    }

    // ─────────────────────────────────────────────────────────
    // Dropdown Query
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateDropdownQuery(CodeTemplate t, ModuleConfig config, String entityName) {
        String labelCol = config.getFields().stream()
                .filter(f -> f.getName().equals(config.getDropdownLabelField()))
                .findFirst()
                .map(FieldConfig::getColumnName)
                .orElse(config.getDropdownLabelField());

        return t.blank()
                .line("// ── Dropdown — lightweight label/value pairs ─────────────")
                .blank()
                .line("@Query(value = \"SELECT e." + config.getUuidField() + " as value, e." +
                        labelCol + " as label FROM " + config.getSchemaName() + "." +
                        config.getTableName() + " e WHERE e.status = 1 ORDER BY e." +
                        labelCol + " ASC\", nativeQuery = true)")
                .line("List<" + entityName + "DropdownProjection> findAllForDropdown();");
    }
    private String getFieldType(ModuleConfig config, String fieldName) {
        if (fieldName.equals(config.getUuidField())) return "UUID";
        if (fieldName.equals(config.getPrimaryKey())) return config.getPrimaryKeyType();

        return config.getFields().stream()
                .filter(f -> f.getName().equals(fieldName))
                .findFirst()
                .map(FieldConfig::getType)
                .orElse("String");
    }
}