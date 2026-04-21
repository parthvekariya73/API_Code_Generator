package com.healthcare.dynamicMaster.v2.generator.runner;

import com.healthcare.dynamicMaster.v2.generator.config.ModuleConfig;
import com.healthcare.dynamicMaster.v2.generator.engine.CodeGeneratorEngine;
import com.healthcare.dynamicMaster.v2.generator.engine.ConfigParser;
import com.healthcare.dynamicMaster.v2.generator.engine.GenerationResult;
import com.healthcare.dynamicMaster.v2.generator.engine.SchemaParserService;
import com.healthcare.dynamicMaster.v2.generator.engine.ZipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * V2 GeneratorRunner — REST Trigger for Code Generation
 * ============================================================
 *
 * HOW TO USE (the easy way):
 *  1. Drop a JSON config in src/main/resources/generator-configs/
 *  2. Call: POST /api/v2/generator/generate?config=product_category
 *  3. Files appear in src/main/java/{packagePath}/
 *
 * Endpoints:
 *   POST /api/v2/generator/generate        → generate from JSON body
 *   POST /api/v2/generator/generate?config={name} → generate from classpath file
 *   POST /api/v2/generator/generate/all    → batch: all configs in directory
 *   POST /api/v2/generator/dry-run         → preview without writing
 *   GET  /api/v2/generator/preview?config={name} → preview single config
 *
 * ACTIVE ONLY in "dev" profile — never exposed in production.
 * Set spring.profiles.active=dev to enable.
 *
 * Output directory: controlled by generator.output-root property.
 * Default: src/main/java
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/generator")
@RequiredArgsConstructor
@Profile("dev") // ← SAFETY: only available in dev profile
@CrossOrigin(origins = "*")
public class GeneratorRunner {

    private final CodeGeneratorEngine engine;
    private final ConfigParser        parser;
    private final SchemaParserService schemaParserService;
    private final ZipService          zipService;

    /**
     * Output root — set in application-dev.yml:
     *   generator:
     *     output-root: src/main/java
     */
    @Value("${generator.output-root:src/main/java}")
    private String outputRoot;

    /**
     * Config directory for batch generation:
     *   generator:
     *     config-dir: src/main/resources/generator-configs
     */
    @Value("${generator.config-dir:src/main/resources/generator-configs}")
    private String configDir;

    // ─────────────────────────────────────────────────────────
    // POST /parse-sql — Convert DDL to ModuleConfig
    // ─────────────────────────────────────────────────────────

    /**
     * Convert a CREATE TABLE SQL statement into a ModuleConfig draft.
     */
    @PostMapping("/parse-sql")
    public ResponseEntity<ModuleConfig> parseSql(@RequestBody String sql) throws Exception {
        log.info("REST Request: Parse SQL DDL");
        ModuleConfig config = schemaParserService.parseSql(sql);
        return ResponseEntity.ok(config);
    }

    // ─────────────────────────────────────────────────────────
    // POST /generate  — from JSON body (most flexible)
    // ─────────────────────────────────────────────────────────

    /**
     * Generate all files for a module defined in the request body.
     *
     * Usage:
     *   POST /api/v2/generator/generate
     *   Content-Type: application/json
     *   Body: { "moduleName": "product_category", ... }
     */
    // @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateFromBody(
            @RequestBody ModuleConfig config,
            @RequestParam(defaultValue = "false") boolean dryRun) {

        log.info("Generator triggered via REST body — module: {}", config.getModuleName());
        List<GenerationResult> results = engine.generate(config, outputRoot, dryRun);
        return ResponseEntity.ok(buildSummary(results, dryRun));
    }

    // ─────────────────────────────────────────────────────────
    // POST /generate/zip — Return ZIP of generated code
    // ─────────────────────────────────────────────────────────

    /**
     * Generate code from config and return it as a ZIP file.
     */
    @PostMapping("/generate/zip")
    public ResponseEntity<Resource> generateZip(@RequestBody ModuleConfig config) throws IOException {
        log.info("ZIP generation triggered for module: {}", config.getModuleName());

        // 1. Generate code (dryRun=true so it doesn't write to server's disk)
        List<GenerationResult> results = engine.generate(config, outputRoot, true);

        // 2. Bundle into ZIP
        byte[] zipBytes = zipService.createZip(results);

        // 3. Prepare response
        ByteArrayResource resource = new ByteArrayResource(zipBytes);
        String filename = config.getModuleName() + "-generated-code.zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipBytes.length)
                .body(resource);
    }

    // ─────────────────────────────────────────────────────────
    // POST /generate?config={name} — from classpath JSON file
    // ─────────────────────────────────────────────────────────

    /**
     * Generate from a named config file in src/main/resources/generator-configs/.
     *
     * Usage:
     *   POST /api/v2/generator/generate?config=product_category
     *   → reads generator-configs/product_category.json
     */
    // @PostMapping(value = "/generate", params = "config")
    public ResponseEntity<Map<String, Object>> generateFromFile(
            @RequestParam String config,
            @RequestParam(defaultValue = "false") boolean dryRun) throws Exception {

        String configPath = "classpath:/generator-configs/" + config + ".json";
        log.info("Generator triggered via file: {}", configPath);

        ModuleConfig moduleConfig = parser.parseConfig(configPath);
        List<GenerationResult> results = engine.generate(moduleConfig, outputRoot, dryRun);
        return ResponseEntity.ok(buildSummary(results, dryRun));
    }

    // ─────────────────────────────────────────────────────────
    // POST /generate/all — batch: all configs in directory
    // ─────────────────────────────────────────────────────────

    /**
     * Generate all modules from all JSON configs in the config directory.
     *
     * Usage:
     *   POST /api/v2/generator/generate/all
     *   → generates all *.json files in generator.config-dir
     */
    // @PostMapping("/generate/all")
    public ResponseEntity<Map<String, Object>> generateAll(
            @RequestParam(defaultValue = "false") boolean dryRun) throws Exception {

        log.info("Batch generation from directory: {}", configDir);
        List<ModuleConfig> configs = parser.parseDirectory(configDir);
        List<GenerationResult> results = engine.generateAll(configs, outputRoot, dryRun);
        return ResponseEntity.ok(buildSummary(results, dryRun));
    }

    // ─────────────────────────────────────────────────────────
    // GET /preview?config={name} — dry-run preview (safe, no writes)
    // ─────────────────────────────────────────────────────────

    /**
     * Preview generated code without writing to disk.
     * Returns list of {filePath, content, lineCount} for each file.
     *
     * Usage:
     *   GET /api/v2/generator/preview?config=product_category
     */
    // @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview(
            @RequestParam String config) throws Exception {

        String configPath = "classpath:/generator-configs/" + config + ".json";
        ModuleConfig moduleConfig = parser.parseConfig(configPath);

        // Always dry-run for preview
        List<GenerationResult> results = engine.generate(moduleConfig, outputRoot, true);

        // Build preview response with full content
        List<Map<String, Object>> files = results.stream()
                .filter(r -> r.getContent() != null)
                .map(r -> {
                    Map<String, Object> file = new LinkedHashMap<>();
                    file.put("component",  r.getComponentType());
                    file.put("filePath",   r.getFilePath());
                    file.put("lineCount",  r.lineCount());
                    file.put("content",    r.getContent());
                    return file;
                })
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("module",    moduleConfig.getModuleName());
        response.put("fileCount", files.size());
        response.put("files",     files);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────
    // Response Builder
    // ─────────────────────────────────────────────────────────

    private Map<String, Object> buildSummary(List<GenerationResult> results, boolean dryRun) {
        long success = results.stream().filter(r -> !r.hasError() && r.getContent() != null).count();
        long errors  = results.stream().filter(GenerationResult::hasError).count();
        int  lines   = results.stream().mapToInt(GenerationResult::lineCount).sum();

        List<Map<String, Object>> fileList = results.stream()
                .map(r -> {
                    Map<String, Object> f = new LinkedHashMap<>();
                    f.put("component", r.getComponentType());
                    f.put("filePath",  r.getFilePath());
                    f.put("written",   r.isWritten());
                    f.put("lineCount", r.lineCount());
                    if (r.hasError()) f.put("error", r.getError());
                    return f;
                })
                .toList();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("dryRun",     dryRun);
        summary.put("totalFiles", results.size());
        summary.put("success",    success);
        summary.put("errors",     errors);
        summary.put("totalLines", lines);
        summary.put("files",      fileList);
        return summary;
    }
}