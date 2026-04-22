package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ValidationRule;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.ImportRegistry;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 EntityGenerator — JPA Entity Class
 * ============================================================
 * Generates a fully annotated JPA @Entity with:
 *  - UUID v7 auto-generation via DB function
 *  - Soft-delete status field
 *  - Full audit trail (createdAt/By, updatedAt/By, deletedAt/By)
 *  - @Table unique constraints from config
 *  - @PrePersist / @PreUpdate lifecycle hooks
 *  - Relationships (@ManyToOne, @OneToMany, etc.)
 *
 * Zero StringBuilder — uses CodeTemplate + ImportRegistry.
 */
@Slf4j
@Component("EntityGeneratorV2")
public class EntityGenerator {

    /**
     * Main entry point.
     * @param config module configuration
     * @return rendered Java source code as String
     */
    public String generate(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating Entity: {}", entityName);

        ImportRegistry imports = buildImports(config);
        String tableAnnotation = buildTableAnnotation(config);

        return CodeTemplate.of()
                // ── Package ──────────────────────────────────────
                .line("package " + config.getPackageName() + ".entity;")
                .blank()
                // ── Imports ──────────────────────────────────────
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                // ── Class Annotations ────────────────────────────
                .line("@Entity")
                .line(tableAnnotation)
                .line("@Getter")
                .line("@Setter")
                .line("@NoArgsConstructor")
                .line("@AllArgsConstructor")
                .line("@Builder")
                // ── Class Body ───────────────────────────────────
                .line("public class " + entityName + " {")
                .blank()
                // ── Primary Key ──────────────────────────────────
                .indent(t -> generatePrimaryKey(t, config))
                .blank()
                // ── UUID Field ───────────────────────────────────
                .indent(t -> generateUuidField(t, config))
                .blank()
                // ── Business Fields ──────────────────────────────
                .indent(t -> generateBusinessFields(t, config))
                // ── Relationships ────────────────────────────────
                .when(!config.getRelationships().isEmpty(), t ->
                        t.indent(tt -> generateRelationships(tt, config)))
                // ── Status Field ─────────────────────────────────
                .indent(t -> generateStatusField(t))
                .blank()
                // ── Audit Fields ─────────────────────────────────
                .indent(t -> generateAuditFields(t))
                .blank()
                // ── Lifecycle Methods ────────────────────────────
                .indent(t -> generateLifecycleMethods(t, config))
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // Import Registry
    // ─────────────────────────────────────────────────────────

    private ImportRegistry buildImports(ModuleConfig config) {
        boolean hasRelationships = !config.getRelationships().isEmpty();

        return new ImportRegistry()
                .addAll(
                        "com.apiCodeGenerator.common.apputil.utils.commonutil.CommonUtil",
                        "com.apiCodeGenerator.common.apputil.utils.commonutil.SecurityUtils",
                        "jakarta.persistence.*",
                        "jakarta.validation.constraints.*",
                        "lombok.*",
                        "org.hibernate.annotations.CreationTimestamp",
                        "org.hibernate.annotations.UpdateTimestamp",
                        "org.hibernate.annotations.Generated",
                        "org.hibernate.generator.EventType",
                        "java.math.BigDecimal",
                        "java.time.LocalDateTime",
                        "java.util.UUID"
                )
                .addForFields(config)
                .addIf(hasRelationships, "java.util.List")
                .addIf(hasRelationships, "java.util.ArrayList");
    }

    // ─────────────────────────────────────────────────────────
    // @Table with unique constraints
    // ─────────────────────────────────────────────────────────

    private String buildTableAnnotation(ModuleConfig config) {
        if (config.getUniqueConstraints().isEmpty()) {
            return "@Table(name = \"" + config.getTableName() +
                    "\", schema = \"" + config.getSchemaName() + "\")";
        }

        // Build uniqueConstraints array
        String constraints = config.getUniqueConstraints().stream()
                .map(uc -> {
                    String cols = uc.getFields().stream()
                            .collect(Collectors.joining("\", \"", "\"", "\""));
                    return "@UniqueConstraint(name = \"" + uc.getName() +
                            "\", columnNames = {" + cols + "})";
                })
                .collect(Collectors.joining(",\n            "));

        return "@Table(\n" +
                "    name = \"" + config.getTableName() + "\",\n" +
                "    schema = \"" + config.getSchemaName() + "\",\n" +
                "    uniqueConstraints = {\n" +
                "        " + constraints + "\n" +
                "    }\n" +
                ")";
    }

    // ─────────────────────────────────────────────────────────
    // Primary Key
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generatePrimaryKey(CodeTemplate t, ModuleConfig config) {
        return t
                .line("// ── Primary Key ─────────────────────────────────────")
                .line("@Id")
                .line("@GeneratedValue(strategy = GenerationType.IDENTITY)")
                .line("@Column(name = \"" + config.getPrimaryKey() + "\")")
                .line("private " + config.getPrimaryKeyType() + " " + config.getPrimaryKey() + ";");
    }

    // ─────────────────────────────────────────────────────────
    // UUID Field (DB-generated UUID v7)
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUuidField(CodeTemplate t, ModuleConfig config) {
        return t
                .line("// ── UUID (DB-generated UUID v7) ─────────────────────")
                .line("@Column(name = \"" + config.getUuidField() + "\",")
                .line("        nullable = false, updatable = false, unique = true,")
                .line("        insertable = false, columnDefinition = \"UUID DEFAULT gen_uuid_v7()\")")
                .line("@Generated(event = EventType.INSERT)")
                .line("private UUID " + config.getUuidField() + ";");
    }

    // ─────────────────────────────────────────────────────────
    // Business Fields
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateBusinessFields(CodeTemplate t, ModuleConfig config) {
        t.line("// ── Business Fields ─────────────────────────────────────");
        for (FieldConfig field : config.getFields()) {
            generateSingleField(t, field);
            t.blank();
        }
        return t;
    }

    private void generateSingleField(CodeTemplate t, FieldConfig field) {
        // Build @Column annotation
        t.line(buildColumnAnnotation(field));

        // Validation annotations
        if (field.getValidations() != null) {
            for (ValidationRule v : field.getValidations()) {
                t.line(buildValidationAnnotation(v));
            }
        }

        // Field declaration
        String javaType = NamingUtil.toJavaType(field.getType());
        t.line("private " + javaType + " " + field.getName() + ";");
    }

    private String buildColumnAnnotation(FieldConfig field) {
        // Collect column attributes into a list, then join — no string mutation
        List<String> attrs = new java.util.ArrayList<>();
        attrs.add("name = \"" + field.getColumnName() + "\"");
        if (!field.isNullable()) attrs.add("nullable = false");
        if (!field.isUpdatable()) attrs.add("updatable = false");
        if (!field.isInsertable()) attrs.add("insertable = false");
        if (field.getLength() != null && "String".equals(field.getType()))
            attrs.add("length = " + field.getLength());
        if (field.getPrecision() != null) attrs.add("precision = " + field.getPrecision());
        if (field.getScale() != null) attrs.add("scale = " + field.getScale());
        if (field.getDefaultValue() != null)
            attrs.add("columnDefinition = \"" + field.getDefaultValue() + "\"");
        if (field.isUnique()) attrs.add("unique = true");

        return "@Column(" + String.join(", ", attrs) + ")";
    }

    private String buildValidationAnnotation(ValidationRule v) {
        if (!v.hasAttributes() && v.getMessage() == null) {
            return "@" + v.getAnnotation();
        }

        List<String> parts = new java.util.ArrayList<>();

        // Attributes first
        v.safeAttributes().entrySet().stream()
                .map(e -> e.getKey() + " = " + NamingUtil.formatAnnotationValue(e.getValue()))
                .forEach(parts::add);

        // Message last
        if (v.getMessage() != null) {
            parts.add("message = \"" + v.getMessage() + "\"");
        }

        return "@" + v.getAnnotation() + "(" + String.join(", ", parts) + ")";
    }

    // ─────────────────────────────────────────────────────────
    // Relationships
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateRelationships(CodeTemplate t, ModuleConfig config) {
        t.line("// ── Relationships ───────────────────────────────────────");
        config.getRelationships().forEach(rel -> {
            t.blank();
            String fetchType = rel.isLazy() ? "FetchType.LAZY" : "FetchType.EAGER";
            String cascade = rel.getCascade() != null ? ", cascade = CascadeType." + rel.getCascade() : "";

            switch (rel.getType()) {
                case "ManyToOne" -> {
                    t.line("@ManyToOne(fetch = " + fetchType + cascade + ")");
                    t.line("@JoinColumn(name = \"" + rel.getJoinColumn() + "\")");
                    t.line("private " + rel.getTargetEntity() + " " + rel.getName() + ";");
                }
                case "OneToMany" -> {
                    t.line("@OneToMany(mappedBy = \"" + rel.getMappedBy() + "\", fetch = " + fetchType + cascade + ")");
                    t.line("@Builder.Default");
                    t.line("private List<" + rel.getTargetEntity() + "> " + rel.getName() + " = new ArrayList<>();");
                }
                case "OneToOne" -> {
                    t.line("@OneToOne(fetch = " + fetchType + cascade + ")");
                    t.line("@JoinColumn(name = \"" + rel.getJoinColumn() + "\")");
                    t.line("private " + rel.getTargetEntity() + " " + rel.getName() + ";");
                }
                case "ManyToMany" -> {
                    t.line("@ManyToMany(fetch = " + fetchType + cascade + ")");
                    t.line("@JoinTable(name = \"" + rel.getJoinColumn() + "\")");
                    t.line("@Builder.Default");
                    t.line("private List<" + rel.getTargetEntity() + "> " + rel.getName() + " = new ArrayList<>();");
                }
            }
        });
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // Status Field
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateStatusField(CodeTemplate t) {
        return t
                .line("// ── Soft-Delete Status: 1=active, 0=inactive, 9=deleted ──")
                .line("@Column(name = \"status\")")
                .line("@Builder.Default")
                .line("private Short status = 1;");
    }

    // ─────────────────────────────────────────────────────────
    // Audit Fields
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateAuditFields(CodeTemplate t) {
        return t
                .line("// ── Audit Trail ─────────────────────────────────────────")
                .line("@CreationTimestamp")
                .line("@Column(name = \"created_at\", updatable = false)")
                .line("private LocalDateTime createdAt;")
                .blank()
                .line("@UpdateTimestamp")
                .line("@Column(name = \"updated_at\")")
                .line("private LocalDateTime updatedAt;")
                .blank()
                .line("@Column(name = \"deleted_at\")")
                .line("private LocalDateTime deletedAt;")
                .blank()
                .line("@Column(name = \"created_by\")")
                .line("private Long createdBy;")
                .blank()
                .line("@Column(name = \"updated_by\")")
                .line("private Long updatedBy;")
                .blank()
                .line("@Column(name = \"deleted_by\")")
                .line("private Long deletedBy;")
                .blank()
                .line("@Column(name = \"audit_tracker_id\", precision = 24)")
                .line("private BigDecimal auditTrackerId;");
    }

    // ─────────────────────────────────────────────────────────
    // Lifecycle Methods
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateLifecycleMethods(CodeTemplate t, ModuleConfig config) {
        return t
                .line("// ── JPA Lifecycle Callbacks ──────────────────────────────")
                .line("@PrePersist")
                .line("protected void onCreate() {")
                .indent(inner -> inner
                        .line("if (this.status == null) this.status = 1;")
                        .line("this.createdBy = SecurityUtils.getCurrentUserId();")
                        .line("this.updatedBy = SecurityUtils.getCurrentUserId();")
                        .line("this.auditTrackerId = CommonUtil.generateUniqueTxnNumber();")
                )
                .line("}")
                .blank()
                .line("@PreUpdate")
                .line("protected void onUpdate() {")
                .indent(inner -> inner
                        .line("this.updatedBy = SecurityUtils.getCurrentUserId();")
                )
                .line("}");
    }
}