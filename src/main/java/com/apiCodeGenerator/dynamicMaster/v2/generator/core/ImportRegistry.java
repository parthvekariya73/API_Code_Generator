package com.apiCodeGenerator.dynamicMaster.v2.generator.core;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 ImportRegistry — Smart, Deduplicating Import Collector
 * ============================================================
 * Collects import FQNs from across all generation steps,
 * deduplicates them, sorts them, and renders them as a block.
 *
 * Avoids duplicated `if (hasXField)` checks scattered
 * across every generator — each generator uses the registry
 * and it handles everything.
 */
public final class ImportRegistry {

    // TreeSet: auto-sorted and de-duplicated
    private final Set<String> imports = new TreeSet<>();

    // ─────────────────────────────────────────────────────────
    // Builder Methods
    // ─────────────────────────────────────────────────────────

    public ImportRegistry add(String fqn) {
        imports.add(fqn);
        return this;
    }

    public ImportRegistry addAll(String... fqns) {
        for (String fqn : fqns) imports.add(fqn);
        return this;
    }

    public ImportRegistry addAll(Collection<String> fqns) {
        imports.addAll(fqns);
        return this;
    }

    public ImportRegistry addIf(boolean condition, String fqn) {
        if (condition) imports.add(fqn);
        return this;
    }

    public ImportRegistry addIf(boolean condition, String... fans) {
        if (condition) {
            imports.addAll(Arrays.asList(fans));
        }
        return this;
    }

    // ─────────────────────────────────────────────────────────
    // Field-Driven Auto-Detection
    // ─────────────────────────────────────────────────────────

    /**
     * Scan all fields in config and add necessary Java type imports.
     * One call covers the entire field list.
     */
    public ImportRegistry addForFields(ModuleConfig config) {
        config.getFields().forEach(field -> addForField(field));
        return this;
    }

    /**
     * Add imports required for a single field's type.
     */
    public ImportRegistry addForField(FieldConfig field) {
        switch (field.getType()) {
            case "LocalDate"     -> imports.add("java.time.LocalDate");
            case "LocalDateTime" -> imports.add("java.time.LocalDateTime");
            case "BigDecimal"    -> imports.add("java.math.BigDecimal");
            case "UUID"          -> imports.add("java.util.UUID");
        }
        // Enum field: add the enum's FQN
        if (field.isEnumField() && field.getEnumClass() != null) {
            imports.add(field.getEnumClass());
        }
        return this;
    }

    /**
     * Add imports required for validation annotations on all fields.
     */
    public ImportRegistry addForValidations(ModuleConfig config) {
        boolean hasValidations = config.getFields().stream()
                .anyMatch(f -> f.getValidations() != null && !f.getValidations().isEmpty());
        if (hasValidations) {
            imports.add("jakarta.validation.constraints.*");
        }
        return this;
    }

    // ─────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────

    /**
     * Render all collected imports as a sorted, deduplicated block.
     * Returns a String ready to be inserted after the package declaration.
     */
    public String render() {
        if (imports.isEmpty()) return "";
        return imports.stream()
                .map(fqn -> "import " + fqn + ";")
                .collect(Collectors.joining("\n"));
    }

    /**
     * Render and append to a CodeTemplate.
     */
    public CodeTemplate appendTo(CodeTemplate template) {
        imports.stream()
                .map(fqn -> "import " + fqn + ";")
                .forEach(template::raw);
        return template;
    }

    /**
     * Get all registered imports (for testing).
     */
    public Set<String> getImports() {
        return new TreeSet<>(imports);
    }
}