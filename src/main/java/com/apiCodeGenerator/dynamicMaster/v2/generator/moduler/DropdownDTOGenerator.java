package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * V2 DropdownDTOGenerator — Dropdown Item Record
 * ============================================================
 * Generates a Java record used as the return type for
 * GET /dropdown endpoints.
 *
 * Why a record (not interface projection)?
 *  - Record is immutable and serializable by default
 *  - Service layer maps projection → record (decouples DB types)
 *  - Jackson serializes records cleanly without extra config
 *
 * Output:
 *   public record ProductCategoryDropdownItem(UUID value, String label) {}
 */
@Slf4j
@Component
public class DropdownDTOGenerator {

    /**
     * Generate the DropdownItem record for a module.
     * Returns null if dropdown is disabled in config.
     */
    public String generate(ModuleConfig config) {
        if (!config.isEnableDropdown()) return null;

        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating DropdownItem: {}DropdownItem", entityName);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".dto.dropdown;")
                .blank()
                .line("import java.util.UUID;")
                .blank()
                .line("/**")
                .line(" * Dropdown option for " + entityName + ".")
                .line(" * Used by GET /dropdown — serialized as {\"value\": UUID, \"label\": String}.")
                .line(" *")
                .line(" * value → UUID of the record (safe public identifier)")
                .line(" * label → human-readable display text (e.g. category name)")
                .line(" */")
                .line("public record " + entityName + "DropdownItem(")
                .indent(t -> t
                        .line("UUID value,")
                        .line("String label")
                )
                .line(") {}")
                .render();
    }
}