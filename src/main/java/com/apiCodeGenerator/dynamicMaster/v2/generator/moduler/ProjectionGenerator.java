package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * V2 ProjectionGenerator — JPA Interface Projections
 * ============================================================
 * Generates:
 *  - {Entity}Projection       — used by paginated list and get-by-uuid
 *  - {Entity}DropdownProjection — lightweight {value, label} pairs
 *
 * Interface projections are used with native SQL queries for
 * maximum performance — no full entity hydration.
 */
@Slf4j
@Component("ProjectionGeneratorV2")
public class ProjectionGenerator {

    /**
     * Generate the main Projection interface.
     */
    public String generate(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating Projection: {}Projection", entityName);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".dto.projection;")
                .blank()
                .line("import java.math.BigDecimal;")
                .line("import java.time.LocalDateTime;")
                .line("import java.util.UUID;")
                .blank()
                .line("/**")
                .line(" * Spring Data JPA interface projection for " + entityName + ".")
                .line(" * Used with native SQL queries — avoids full entity hydration.")
                .line(" */")
                .line("public interface " + entityName + "Projection {")
                .blank()
                .indent(t -> {
                    // PK and UUID
                    t.line("/** Internal primary key (not exposed in API responses) */");
                    t.line("Long get" + NamingUtil.capitalize(config.getPrimaryKey()) + "();");
                    t.blank();
                    t.line("/** Public UUID identifier */");
                    t.line("UUID get" + NamingUtil.capitalize(config.getUuidField()) + "();");
                    t.blank();

                    // Business fields
                    t.line("// ── Business Fields ──────────────────────────────────────");
                    for (FieldConfig field : config.getFields()) {
                        if (!field.isExcludeFromProjection()) {
                            t.line(NamingUtil.toJavaType(field.getType()) +
                                    " " + field.getterName() + "();");
                        }
                    }
                    t.blank();

                    // System + audit
                    t.line("// ── System / Audit Fields ────────────────────────────────");
                    t.line("String getStatus();");
                    t.line("LocalDateTime getCreatedAt();");
                    t.line("LocalDateTime getUpdatedAt();");
                    t.line("String getCreatedBy();");
                    t.line("String getUpdatedBy();");
                    t.line("BigDecimal getAuditTrackerId();");
                    return t;
                })
                .line("}")
                .render();
    }

    /**
     * Generate the lightweight Dropdown Projection interface.
     * Used for select/autocomplete components in the UI.
     */
    public String generateDropdownProjection(ModuleConfig config) {
        if (!config.isEnableDropdown()) return null;

        String entityName = NamingUtil.toEntityName(config);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".dto.projection;")
                .blank()
                .line("import java.util.UUID;")
                .blank()
                .line("/**")
                .line(" * Lightweight projection for dropdown/select options.")
                .line(" * Returns only value (UUID) and label (display name).")
                .line(" */")
                .line("public interface " + entityName + "DropdownProjection {")
                .blank()
                .indent(t -> t
                        .line("UUID getValue();")
                        .line("String getLabel();")
                )
                .line("}")
                .render();
    }
}