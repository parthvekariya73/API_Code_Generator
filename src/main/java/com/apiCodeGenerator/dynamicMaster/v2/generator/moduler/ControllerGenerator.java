package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.ImportRegistry;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 ControllerGenerator — Spring REST Controller
 * ============================================================
 * Generates a @RestController with:
 *  - POST   /api/v1/{path}           → create
 *  - PUT    /api/v1/{path}/{uuid}    → update
 *  - GET    /api/v1/{path}/{uuid}    → getByUuid
 *  - GET    /api/v1/{path}           → getAll (paginated + search + filter)
 *  - DELETE /api/v1/{path}/{uuid}    → delete
 *  - PATCH  /api/v1/{path}/{uuid}/status → updateStatus
 *  - GET    /api/v1/{path}/count     → countActive  (optional)
 *  - GET    /api/v1/{path}/dropdown  → dropdown     (optional)
 *
 * All endpoints include:
 *  - @Operation Swagger documentation
 *  - Structured ApiResponse wrapper
 *  - HttpServletRequest for request-ID tracking
 */
@Slf4j
@Component("controllerGeneratorV2")
public class ControllerGenerator {

    public String generate(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        String restPath   = NamingUtil.toRestPath(config);
        log.info("Generating Controller: {}Controller", entityName);

        List<FieldConfig> filterableFields = config.getFields().stream()
                .filter(FieldConfig::isFilterable).toList();

        ImportRegistry imports = buildImports(config, entityName);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".controller;")
                .blank()
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                .line("/**")
                .line(" * REST Controller for " + entityName + " master data management.")
                .line(" * Base path: /api/v1/" + restPath)
                .line(" */")
                .line("@Tag(name = \"" + entityName + " Management\")")
                .line("@Slf4j")
                .line("@RestController")
                .line("@RequestMapping(\"/api/v1/" + restPath + "\")")
                .line("@RequiredArgsConstructor")
                .line("public class " + entityName + "Controller {")
                .blank()
                .indent(t -> t.line("private final " + entityName + "Service service;"))
                .blank()
                // ── create ────────────────────────────────────────
                .indent(t -> generateCreate(t, entityName))
                .blank()
                // ── update ────────────────────────────────────────
                .indent(t -> generateUpdate(t, entityName))
                .blank()
                // ── getByUuid ─────────────────────────────────────
                .indent(t -> generateGetByUuid(t, entityName))
                .blank()
                // ── getAll ────────────────────────────────────────
                .indent(t -> generateGetAll(t, config, entityName, filterableFields))
                .blank()
                // ── delete ────────────────────────────────────────
                .indent(t -> generateDelete(t, entityName))
                .blank()
                // ── updateStatus ──────────────────────────────────
                .indent(t -> generateUpdateStatus(t, entityName))
                // ── countActive ───────────────────────────────────
                .when(config.isEnableCount(), t ->
                        t.blank().indent(tt -> generateCount(tt, entityName)))
                // ── dropdown ──────────────────────────────────────
                .when(config.isEnableDropdown(), t ->
                        t.blank().indent(tt -> generateDropdown(tt, entityName)))
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // POST → create
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateCreate(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Create a new " + entityName + "\")")
                .line("@PostMapping")
                .line("public ResponseEntity<ApiResponse<" + entityName + "ResponseDTO>> create(")
                .line("        @Valid @RequestBody " + entityName + "RequestDTO requestDTO,")
                .line("        HttpServletRequest httpRequest) {")
                .blank()
                .indent(inner -> inner
                        .line(entityName + "ResponseDTO response = service.create(requestDTO);")
                        .line("return ResponseEntity.status(HttpStatus.CREATED)")
                        .line("        .body(ApiResponse.success(")
                        .line("                \"" + entityName + " created successfully\",")
                        .line("                response,")
                        .line("                CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // PUT /{uuid} → update
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUpdate(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Update " + entityName + " by UUID\")")
                .line("@PutMapping(\"/{uuid}\")")
                .line("public ResponseEntity<ApiResponse<" + entityName + "ResponseDTO>> update(")
                .line("        @PathVariable UUID uuid,")
                .line("        @Valid @RequestBody " + entityName + "RequestDTO requestDTO,")
                .line("        HttpServletRequest httpRequest) {")
                .blank()
                .indent(inner -> inner
                        .line(entityName + "ResponseDTO response = service.update(uuid, requestDTO);")
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " updated successfully\",")
                        .line("        response,")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // GET /{uuid} → getByUuid
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateGetByUuid(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Get " + entityName + " by UUID\")")
                .line("@GetMapping(\"/{uuid}\")")
                .line("public ResponseEntity<ApiResponse<" + entityName + "ResponseDTO>> getByUuid(")
                .line("        @PathVariable UUID uuid,")
                .line("        HttpServletRequest httpRequest) {")
                .blank()
                .indent(inner -> inner
                        .line(entityName + "ResponseDTO response = service.getByUuid(uuid);")
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " retrieved successfully\",")
                        .line("        response,")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // GET / → getAll (paginated + search + optional filters)
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateGetAll(CodeTemplate t, ModuleConfig config,
                                        String entityName, List<FieldConfig> filterableFields) {
        String filterParams = filterableFields.stream()
                .map(f -> "\n        @RequestParam(required = false) String " + f.getName() + ",")
                .collect(Collectors.joining());

        String serviceArgs = filterableFields.stream()
                .map(f -> ", " + f.getName())
                .collect(Collectors.joining());

        return t
                .line("@Operation(summary = \"List " + entityName + " with pagination, search, and filters\")")
                .line("@GetMapping")
                .line("public ResponseEntity<ApiResponse<Page<" + entityName + "ResponseDTO>>> getAll(")
                .line("        @RequestParam(defaultValue = \"1\") int page,")
                .line("        @RequestParam(defaultValue = \"" + config.getDefaultPageSize() + "\") int size,")
                .line("        @RequestParam(required = false) String search," + filterParams)
                .line("        HttpServletRequest httpRequest) {")
                .blank()
                .indent(inner -> inner
                        .line("Page<" + entityName + "ResponseDTO> response = service.getAll(page - 1, size, search" + serviceArgs + ");")
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " list retrieved successfully\",")
                        .line("        response,")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // DELETE /{uuid} → soft delete
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateDelete(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Delete " + entityName + " by UUID\")")
                .line("@DeleteMapping(\"/{uuid}\")")
                .line("public ResponseEntity<ApiResponse<Void>> delete(")
                .line("        @PathVariable UUID uuid,")
                .line("        HttpServletRequest httpRequest) {")
                .blank()
                .indent(inner -> inner
                        .line("service.delete(uuid);")
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " deleted successfully\",")
                        .line("        null,")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // PATCH /{uuid}/status → updateStatus
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUpdateStatus(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Toggle " + entityName + " active/inactive status\")")
                .line("@PatchMapping(\"/{uuid}/status\")")
                .line("public ResponseEntity<ApiResponse<" + entityName + "ResponseDTO>> updateStatus(")
                .line("        @PathVariable UUID uuid,")
                .line("        @RequestParam String status,")
                .line("        HttpServletRequest httpRequest) {")
                .blank()
                .indent(inner -> inner
                        .line(entityName + "ResponseDTO response = service.updateStatus(uuid, status);")
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " status updated successfully\",")
                        .line("        response,")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // GET /count → countActive
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateCount(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Count all active " + entityName + " records\")")
                .line("@GetMapping(\"/count\")")
                .line("public ResponseEntity<ApiResponse<Long>> countActive(HttpServletRequest httpRequest) {")
                .indent(inner -> inner
                        .line("long count = service.countActive();")
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " count retrieved\",")
                        .line("        count,")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // GET /dropdown → dropdown options
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateDropdown(CodeTemplate t, String entityName) {
        return t
                .line("@Operation(summary = \"Get " + entityName + " dropdown options\")")
                .line("@GetMapping(\"/dropdown\")")
                .line("public ResponseEntity<ApiResponse<List<" + entityName + "DropdownItem>>> getDropdown(")
                .line("        HttpServletRequest httpRequest) {")
                .indent(inner -> inner
                        .line("return ResponseEntity.ok(ApiResponse.success(")
                        .line("        \"" + entityName + " dropdown retrieved\",")
                        .line("        service.getDropdown(),")
                        .line("        CommonUtil.getRequestId(httpRequest)));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // Imports
    // ─────────────────────────────────────────────────────────

    private ImportRegistry buildImports(ModuleConfig config, String entityName) {
        String pkg = config.getPackageName();
        return new ImportRegistry()
                .add(pkg + ".dto.request." + entityName + "RequestDTO")
                .add(pkg + ".dto.response." + entityName + "ResponseDTO")
                .add(pkg + ".service." + entityName + "Service")
                .addAll(
                        "com.apiCodeGenerator.common.apputil.response.ApiResponse",
                        "com.apiCodeGenerator.common.apputil.utils.commonutil.CommonUtil",
                        "io.swagger.v3.oas.annotations.Operation",
                        "io.swagger.v3.oas.annotations.tags.Tag",
                        "jakarta.servlet.http.HttpServletRequest",
                        "jakarta.validation.Valid",
                        "lombok.RequiredArgsConstructor",
                        "lombok.extern.slf4j.Slf4j",
                        "org.springframework.data.domain.Page",
                        "org.springframework.http.HttpStatus",
                        "org.springframework.http.ResponseEntity",
                        "org.springframework.web.bind.annotation.*",
                        "java.util.List",
                        "java.util.UUID"
                );
    }
}