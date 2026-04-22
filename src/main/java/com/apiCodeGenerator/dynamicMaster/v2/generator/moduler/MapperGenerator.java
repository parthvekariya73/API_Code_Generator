package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.ImportRegistry;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ============================================================
 * V2 MapperGenerator — Entity ↔ DTO Mapper
 * ============================================================
 * Generates a Spring @Component mapper class with:
 *  - toEntity(RequestDTO)   → new Entity instance
 *  - updateEntity(Entity, RequestDTO) → partial update (null-safe)
 *  - toResponseDTO(Projection) → outbound DTO
 *
 * Uses null-safe updates: if the DTO field is null, the
 * existing entity field is preserved (PATCH semantics).
 */
@Slf4j
@Component("MapperGeneratorV2")
public class MapperGenerator {

    public String generate(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating Mapper: {}Mapper", entityName);

        ImportRegistry imports = buildImports(config, entityName);

        List<FieldConfig> requestFields = config.getFields().stream()
                .filter(f -> !f.isExcludeFromRequest())
                .toList();

        List<FieldConfig> responseFields = config.getFields().stream()
                .filter(f -> !f.isExcludeFromResponse())
                .toList();

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".mapper;")
                .blank()
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                .line("/**")
                .line(" * Mapper for " + entityName + " — converts between entity and DTOs.")
                .line(" * All update operations are null-safe (support partial updates).")
                .line(" */")
                .line("@Slf4j")
                .line("@Component")
                .line("@RequiredArgsConstructor")
                .line("public class " + entityName + "Mapper {")
                .blank()
                // toEntity
                .indent(t -> generateToEntity(t, config, entityName, requestFields))
                .blank()
                // updateEntity
                .indent(t -> generateUpdateEntity(t, config, entityName, requestFields))
                .blank()
                // toResponseDTO
                .indent(t -> generateToResponseDTO(t, config, entityName, responseFields))
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // toEntity
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateToEntity(CodeTemplate t, ModuleConfig config,
                                          String entityName, List<FieldConfig> fields) {
        t.line("/**")
                .line(" * Map RequestDTO → new Entity.")
                .line(" * Status is initialized to 1 (active).")
                .line(" */")
                .line("public " + entityName + " toEntity(" + entityName + "RequestDTO dto) {")
                .indent(inner -> {
                    inner.line("return " + entityName + ".builder()");
                    inner.indent2(b -> {
                        for (FieldConfig field : fields) {
                            b.line("." + field.getName() + "(dto." + field.getterName() + "())");
                        }
                        b.line(".status((short) 1)");
                        b.line(".build();");
                        return b;
                    });
                    return inner;
                })
                .line("}");
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // updateEntity (null-safe partial update)
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUpdateEntity(CodeTemplate t, ModuleConfig config,
                                              String entityName, List<FieldConfig> fields) {
        t.line("/**")
                .line(" * Apply changes from RequestDTO to existing Entity.")
                .line(" * Null fields in DTO are skipped — preserves current values.")
                .line(" */")
                .line("public void updateEntity(" + entityName + " entity, " + entityName + "RequestDTO dto) {");

        t.indent(inner -> {
            for (FieldConfig field : fields) {
                // null-safe guard for object types; primitives always set
                boolean isPrimitive = List.of("int","long","boolean","byte","short","float","double")
                        .contains(field.getType().toLowerCase());

                if (isPrimitive) {
                    inner.line("entity." + field.setterName() + "(dto." + field.getterName() + "());");
                } else {
                    inner.line("if (dto." + field.getterName() + "() != null) {");
                    inner.indent(guard -> guard
                            .line("entity." + field.setterName() + "(dto." + field.getterName() + "());")
                    );
                    inner.line("}");
                }
            }
            return inner;
        });

        t.line("}");
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // toResponseDTO
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateToResponseDTO(CodeTemplate t, ModuleConfig config,
                                               String entityName, List<FieldConfig> fields) {
        String uuid = config.getUuidField();

        t.line("/**")
                .line(" * Map Projection → ResponseDTO.")
                .line(" * Returns null if projection is null.")
                .line(" */")
                .line("public " + entityName + "ResponseDTO toResponseDTO(" + entityName + "Projection projection) {")
                .indent(inner -> {
                    inner.line("if (projection == null) return null;");
                    inner.blank();
                    inner.line("return " + entityName + "ResponseDTO.builder()");
                    inner.indent2(b -> {
                        b.line("." + uuid + "(projection." + NamingUtil.getter(uuid) + "())");
                        for (FieldConfig field : fields) {
                            b.line("." + field.getName() + "(projection." + field.getterName() + "())");
                        }
                        b.line(".status(projection.getStatus())");
                        b.line(".createdAt(projection.getCreatedAt())");
                        b.line(".updatedAt(projection.getUpdatedAt())");
                        b.line(".createdBy(projection.getCreatedBy())");
                        b.line(".updatedBy(projection.getUpdatedBy())");
                        b.line(".auditTrackerId(projection.getAuditTrackerId())");
                        b.line(".build();");
                        return b;
                    });
                    return inner;
                })
                .line("}");
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // Imports
    // ─────────────────────────────────────────────────────────

    private ImportRegistry buildImports(ModuleConfig config, String entityName) {
        String pkg = config.getPackageName();
        return new ImportRegistry()
                .add(pkg + ".dto.projection." + entityName + "Projection")
                .add(pkg + ".dto.request." + entityName + "RequestDTO")
                .add(pkg + ".dto.response." + entityName + "ResponseDTO")
                .add(pkg + ".entity." + entityName)
                .addAll(
                        "lombok.RequiredArgsConstructor",
                        "lombok.extern.slf4j.Slf4j",
                        "org.springframework.stereotype.Component"
                );
    }
}