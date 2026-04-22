package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ValidationRule;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.ImportRegistry;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * V2 DTOGenerator — Request DTO, Response DTO
 * ============================================================
 * Generates:
 *  - {Entity}RequestDTO  — validated inbound payload
 *  - {Entity}ResponseDTO — outbound API response shape
 *
 * Fields marked excludeFromRequest / excludeFromResponse
 * are automatically omitted from the respective DTO.
 */
@Slf4j
@Component("DTOGeneratorV2")
public class DTOGenerator {

    // ─────────────────────────────────────────────────────────
    // Request DTO
    // ─────────────────────────────────────────────────────────

    public String generateRequestDTO(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating RequestDTO: {}RequestDTO", entityName);

        List<FieldConfig> requestFields = config.getFields().stream()
                .filter(f -> !f.isExcludeFromRequest())
                .toList();

        ImportRegistry imports = buildRequestImports(config, requestFields);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".dto.request;")
                .blank()
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                .line("/**")
                .line(" * Request DTO for " + entityName + " create/update operations.")
                .line(" * All fields are validated via Jakarta Validation.")
                .line(" */")
                .line("@Data")
                .line("@Builder")
                .line("@NoArgsConstructor")
                .line("@AllArgsConstructor")
                .line("public class " + entityName + "RequestDTO {")
                .blank()
                .indent(t -> {
                    for (FieldConfig field : requestFields) {
                        generateRequestField(t, field);
                        t.blank();
                    }
                    return t;
                })
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // Response DTO
    // ─────────────────────────────────────────────────────────

    public String generateResponseDTO(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating ResponseDTO: {}ResponseDTO", entityName);

        List<FieldConfig> responseFields = config.getFields().stream()
                .filter(f -> !f.isExcludeFromResponse())
                .toList();

        ImportRegistry imports = buildResponseImports(config, responseFields);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".dto.response;")
                .blank()
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                .line("/**")
                .line(" * Response DTO for " + entityName + ".")
                .line(" * Returned by all API endpoints. Includes audit metadata.")
                .line(" */")
                .line("@Data")
                .line("@Builder")
                .line("@NoArgsConstructor")
                .line("@AllArgsConstructor")
                .line("public class " + entityName + "ResponseDTO {")
                .blank()
                .indent(t -> {
                    // UUID identifier
                    t.line("/** Public identifier — use this in all client-side references */");
                    t.line("private UUID " + config.getUuidField() + ";");
                    t.blank();

                    // Business fields
                    for (FieldConfig field : responseFields) {
                        t.line("private " + NamingUtil.toJavaType(field.getType()) + " " + field.getName() + ";");
                    }
                    t.blank();

                    // System fields
                    t.line("/** Current status: 'active' | 'inactive' | 'deleted' */");
                    t.line("private String status;");
                    t.blank();

                    // Audit fields
                    t.line("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")");
                    t.line("private LocalDateTime createdAt;");
                    t.blank();
                    t.line("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")");
                    t.line("private LocalDateTime updatedAt;");
                    t.blank();
                    t.line("private String createdBy;");
                    t.line("private String updatedBy;");
                    t.blank();
                    t.line("private BigDecimal auditTrackerId;");
                    return t;
                })
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // Field Generation
    // ─────────────────────────────────────────────────────────

    private void generateRequestField(CodeTemplate t, FieldConfig field) {
        // Validation annotations
        if (field.getValidations() != null) {
            for (ValidationRule v : field.getValidations()) {
                t.line(buildValidationAnnotation(v));
            }
        }

        // Date format annotation
        if ("LocalDate".equals(field.getType())) {
            t.line("@JsonFormat(pattern = \"yyyy-MM-dd\")");
        } else if ("LocalDateTime".equals(field.getType())) {
            t.line("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")");
        }

        // Field declaration
        t.line("private " + NamingUtil.toJavaType(field.getType()) + " " + field.getName() + ";");
    }

    private String buildValidationAnnotation(ValidationRule v) {
        if (!v.hasAttributes() && v.getMessage() == null) {
            return "@" + v.getAnnotation();
        }

        List<String> parts = new ArrayList<>();
        v.safeAttributes().entrySet().stream()
                .map(e -> e.getKey() + " = " + NamingUtil.formatAnnotationValue(e.getValue()))
                .forEach(parts::add);
        if (v.getMessage() != null) {
            parts.add("message = \"" + v.getMessage() + "\"");
        }
        return "@" + v.getAnnotation() + "(" + String.join(", ", parts) + ")";
    }

    // ─────────────────────────────────────────────────────────
    // Import Builders
    // ─────────────────────────────────────────────────────────

    private ImportRegistry buildRequestImports(ModuleConfig config, List<FieldConfig> fields) {
        boolean hasDate = fields.stream().anyMatch(FieldConfig::isDateField);

        return new ImportRegistry()
                .addAll(
                        "com.fasterxml.jackson.annotation.JsonFormat",
                        "jakarta.validation.constraints.*",
                        "lombok.*",
                        "java.util.UUID"
                )
                .addForFields(config)
                .addIf(hasDate, "com.fasterxml.jackson.annotation.JsonFormat");
    }

    private ImportRegistry buildResponseImports(ModuleConfig config, List<FieldConfig> fields) {
        return new ImportRegistry()
                .addAll(
                        "com.fasterxml.jackson.annotation.JsonFormat",
                        "lombok.*",
                        "java.math.BigDecimal",
                        "java.time.LocalDateTime",
                        "java.util.UUID"
                )
                .addForFields(config);
    }
}