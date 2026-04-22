package com.apiCodeGenerator.dynamicMaster.v2.generator.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * V2 ConfigParser — JSON → ModuleConfig with Validation
 * ============================================================
 * Parses one or many JSON config files and validates them
 * before passing to the generation engine.
 *
 * Supports:
 *  - Single file path
 *  - Directory of config files (batch generation)
 *  - Classpath resources
 */
@Slf4j
@Component
public class ConfigParser {

    private final ObjectMapper objectMapper;

    public ConfigParser() {
        this.objectMapper = new ObjectMapper();
        // Pretty-print for better error messages
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ignore unknown fields — forward compatibility
        this.objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
        );
    }

    // ─────────────────────────────────────────────────────────
    // Parse Single Config
    // ─────────────────────────────────────────────────────────

    /**
     * Parse a single config file from a path string.
     * Supports both filesystem paths and classpath: prefix.
     */
    public ModuleConfig parseConfig(String configPath) throws Exception {
        log.info("Parsing config: {}", configPath);

        ModuleConfig config;

        if (configPath.startsWith("classpath:")) {
            // Load from classpath
            String resource = configPath.substring("classpath:".length());
            try (InputStream is = getClass().getResourceAsStream(resource)) {
                if (is == null) throw new IllegalArgumentException("Classpath resource not found: " + resource);
                config = objectMapper.readValue(is, ModuleConfig.class);
            }
        } else {
            File file = new File(configPath);
            if (!file.exists()) throw new IllegalArgumentException("Config file not found: " + configPath);
            config = objectMapper.readValue(file, ModuleConfig.class);
        }

        validate(config);
        return config;
    }

    // ─────────────────────────────────────────────────────────
    // Parse Directory (Batch Mode)
    // ─────────────────────────────────────────────────────────

    /**
     * Parse all *.json config files in a directory.
     * Returns a list of valid configs; invalid files are skipped
     * with a warning (not fail-fast) to allow partial batch runs.
     */
    public List<ModuleConfig> parseDirectory(String dirPath) throws Exception {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + dirPath);
        }

        File[] jsonFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            log.warn("No JSON config files found in: {}", dirPath);
            return List.of();
        }

        List<ModuleConfig> configs = new ArrayList<>();
        for (File f : jsonFiles) {
            try {
                ModuleConfig config = objectMapper.readValue(f, ModuleConfig.class);
                validate(config);
                configs.add(config);
                log.info("Parsed config: {} → module '{}'", f.getName(), config.getModuleName());
            } catch (Exception ex) {
                log.error("Failed to parse config file '{}': {}", f.getName(), ex.getMessage());
                // Continue batch — don't abort on single bad file
            }
        }
        return configs;
    }

    // ─────────────────────────────────────────────────────────
    // Validation
    // ─────────────────────────────────────────────────────────

    /**
     * Validate a parsed ModuleConfig.
     * Throws IllegalArgumentException if required fields are missing.
     */
    private void validate(ModuleConfig config) {
        List<String> errors = new ArrayList<>();

        if (isBlank(config.getModuleName()))   errors.add("moduleName is required");
        if (isBlank(config.getPackageName()))   errors.add("packageName is required");
        if (isBlank(config.getTableName()))     errors.add("tableName is required");
        if (isBlank(config.getSchemaName()))    errors.add("schemaName is required");
        if (isBlank(config.getPrimaryKey()))    errors.add("primaryKey is required");
        if (isBlank(config.getUuidField()))     errors.add("uuidField is required");

        if (config.getFields() == null || config.getFields().isEmpty()) {
            errors.add("At least one field is required");
        } else {
            config.getFields().forEach(field -> {
                if (isBlank(field.getName()))       errors.add("Field missing 'name'");
                if (isBlank(field.getColumnName())) errors.add("Field '" + field.getName() + "' missing 'columnName'");
                if (isBlank(field.getType()))       errors.add("Field '" + field.getName() + "' missing 'type'");
            });
        }

        // Dropdown label field must be an actual field name if specified
        if (config.getDropdownLabelField() != null && config.getFields() != null) {
            boolean found = config.getFields().stream()
                    .anyMatch(f -> f.getName().equals(config.getDropdownLabelField()));
            if (!found) {
                errors.add("dropdownLabelField '" + config.getDropdownLabelField() + "' not found in fields list");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Config validation failed for module '" + config.getModuleName() + "':\n  - " +
                            String.join("\n  - ", errors)
            );
        }

        log.debug("Config validated: module='{}', fields={}, uniqueConstraints={}",
                config.getModuleName(),
                config.getFields().size(),
                config.getUniqueConstraints().size()
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}