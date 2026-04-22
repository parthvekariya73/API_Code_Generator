package com.apiCodeGenerator.dynamicMaster.v2.generator.engine;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import com.apiCodeGenerator.dynamicMaster.v2.generator.moduler.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * V2 CodeGeneratorEngine — Main Orchestrator
 * ============================================================
 * Wires together every generator module and writes output files.
 *
 * Generation order (dependency-safe):
 *  1. Entity
 *  2. Projection (Main + Dropdown)
 *  3. Request DTO
 *  4. Response DTO
 *  5. Dropdown DTO (record)
 *  6. Repository
 *  7. Mapper
 *  8. Service Interface
 *  9. ServiceImpl
 * 10. Controller
 *
 * Output structure mirrors standard Spring Boot layout:
 *   {outputRoot}/{packagePath}/
 *     entity/
 *     dto/request/  dto/response/  dto/projection/  dto/dropdown/
 *     repository/
 *     mapper/
 *     service/  service/impl/
 *     controller/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeGeneratorEngine {

    // ── Injected Generators ────────────────────────────────────
    private final EntityGenerator       entityGenerator;
    private final RepositoryGenerator   repositoryGenerator;
    private final DTOGenerator          dtoGenerator;
    private final ProjectionGenerator   projectionGenerator;
    private final MapperGenerator       mapperGenerator;
    private final ServiceGenerator      serviceGenerator;
    private final ControllerGenerator   controllerGenerator;

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Generate all files for a module and write them to disk.
     *
     * @param config     parsed module configuration
     * @param outputRoot root output directory (e.g. "src/main/java")
     * @param dryRun     if true, collect results but do NOT write to disk
     * @return list of GenerationResult (one per generated file)
     */
    public List<GenerationResult> generate(ModuleConfig config, String outputRoot, boolean dryRun) {
        String entityName = NamingUtil.toEntityName(config);
        String basePath   = buildBasePath(outputRoot, config.getPackageName());

        log.info("═══════════════════════════════════════════════════");
        log.info("  Generating module: {}  (dryRun={})", entityName, dryRun);
        log.info("  Output root : {}", outputRoot);
        log.info("  Base path   : {}", basePath);
        log.info("═══════════════════════════════════════════════════");

        List<GenerationResult> results = new ArrayList<>();

        // ── 1. Entity ──────────────────────────────────────────
        results.add(generate(config, entityName, "Entity",
                entityGenerator.generate(config),
                basePath + "/entity/" + entityName + ".java",
                dryRun));

        // ── 2. Projection ──────────────────────────────────────
        results.add(generate(config, entityName, "Projection",
                projectionGenerator.generate(config),
                basePath + "/dto/projection/" + entityName + "Projection.java",
                dryRun));

        if (config.isEnableDropdown()) {
            results.add(generate(config, entityName, "DropdownProjection",
                    projectionGenerator.generateDropdownProjection(config),
                    basePath + "/dto/projection/" + entityName + "DropdownProjection.java",
                    dryRun));
        }

        // ── 3. Request DTO ─────────────────────────────────────
        results.add(generate(config, entityName, "RequestDTO",
                dtoGenerator.generateRequestDTO(config),
                basePath + "/dto/request/" + entityName + "RequestDTO.java",
                dryRun));

        // ── 4. Response DTO ────────────────────────────────────
        results.add(generate(config, entityName, "ResponseDTO",
                dtoGenerator.generateResponseDTO(config),
                basePath + "/dto/response/" + entityName + "ResponseDTO.java",
                dryRun));

        // ── 5. Dropdown DTO ────────────────────────────────────
//        if (config.isEnableDropdown()) {
//            results.add(generate(config, entityName, "DropdownItem",
//                    dropdownDTOGenerator.generate(config),
//                    basePath + "/dto/dropdown/" + entityName + "DropdownItem.java",
//                    dryRun));
//        }

        // ── 6. Repository ──────────────────────────────────────
        results.add(generate(config, entityName, "Repository",
                repositoryGenerator.generate(config),
                basePath + "/repository/" + entityName + "Repository.java",
                dryRun));

        // ── 7. Mapper ──────────────────────────────────────────
        results.add(generate(config, entityName, "Mapper",
                mapperGenerator.generate(config),
                basePath + "/mapper/" + entityName + "Mapper.java",
                dryRun));

        // ── 8. Service Interface ───────────────────────────────
        results.add(generate(config, entityName, "Service",
                serviceGenerator.generateInterface(config),
                basePath + "/service/" + entityName + "Service.java",
                dryRun));

        // ── 9. ServiceImpl ─────────────────────────────────────
        results.add(generate(config, entityName, "ServiceImpl",
                serviceGenerator.generateImpl(config),
                basePath + "/service/impl/" + entityName + "ServiceImpl.java",
                dryRun));

        // ── 10. Controller ─────────────────────────────────────
        results.add(generate(config, entityName, "Controller",
                controllerGenerator.generate(config),
                basePath + "/controller/" + entityName + "Controller.java",
                dryRun));

        // ── Summary ────────────────────────────────────────────
        long written = results.stream().filter(GenerationResult::isWritten).count();
        long errors  = results.stream().filter(GenerationResult::hasError).count();

        log.info("═══════════════════════════════════════════════════");
        log.info("  Module '{}' complete: {} files | {} errors",
                entityName, written, errors);
        log.info("═══════════════════════════════════════════════════");

        return results;
    }

    /**
     * Convenience: generate for multiple configs (batch mode).
     */
    public List<GenerationResult> generateAll(List<ModuleConfig> configs,
                                              String outputRoot,
                                              boolean dryRun) {
        List<GenerationResult> all = new ArrayList<>();
        for (ModuleConfig config : configs) {
            try {
                all.addAll(generate(config, outputRoot, dryRun));
            } catch (Exception ex) {
                log.error("Failed to generate module '{}': {}", config.getModuleName(), ex.getMessage(), ex);
                all.add(GenerationResult.builder()
                        .componentType("Module")
                        .filePath(config.getModuleName())
                        .error(ex.getMessage())
                        .written(false)
                        .build());
            }
        }
        return all;
    }

    // ─────────────────────────────────────────────────────────
    // Internal Helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Generate a single component, optionally write to disk.
     * Any generation error is caught and stored in the result.
     */
    private GenerationResult generate(ModuleConfig config,
                                      String entityName,
                                      String componentType,
                                      String content,
                                      String filePath,
                                      boolean dryRun) {
        GenerationResult.GenerationResultBuilder result = GenerationResult.builder()
                .componentType(componentType)
                .filePath(filePath)
                .content(content);

        if (content == null) {
            // Generator returned null (e.g. dropdown disabled)
            result.written(false).error("Generator returned null — skipped");
            return result.build();
        }

        if (dryRun) {
            log.info("  [DRY-RUN] {} → {} ({} lines)", componentType, filePath, content.split("\n").length);
            result.written(false);
            return result.build();
        }

        try {
            writeFile(filePath, content);
            log.info("  ✓ {} → {}", componentType, filePath);
            result.written(true);
        } catch (IOException ex) {
            log.error("  ✗ {} → {} FAILED: {}", componentType, filePath, ex.getMessage());
            result.written(false).error(ex.getMessage());
        }

        return result.build();
    }

    /**
     * Write content to a file, creating parent directories as needed.
     */
    private void writeFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Convert package name + output root to a filesystem path.
     * "com.healthcare.product" → "{root}/com/healthcare/product"
     */
    private String buildBasePath(String outputRoot, String packageName) {
        return outputRoot + "/" + NamingUtil.packageToPath(packageName);
    }
}