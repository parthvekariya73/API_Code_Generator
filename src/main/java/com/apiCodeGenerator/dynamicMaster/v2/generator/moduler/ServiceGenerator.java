package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.UniqueConstraint;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.CodeTemplate;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.ImportRegistry;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 ServiceGenerator — Service Interface + ServiceImpl
 * ============================================================
 * Generates:
 *  - {Entity}Service interface — contract
 *  - {Entity}ServiceImpl      — full implementation with:
 *      · Unique constraint validation on create and update
 *      · Soft delete via softDeleteById
 *      · Status toggle
 *      · Paginated getAll with search + filter params
 *      · Count active records
 *      · Dropdown list
 *      · Optional Spring @Cacheable / @CacheEvict
 */
@Slf4j
@Component("ServiceGeneratorV2")
public class ServiceGenerator {

    // ─────────────────────────────────────────────────────────
    // Service Interface
    // ─────────────────────────────────────────────────────────

    public String generateInterface(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating Service Interface: {}Service", entityName);

        List<FieldConfig> filterableFields = config.getFields().stream()
                .filter(FieldConfig::isFilterable).toList();

        String getAllParams = buildGetAllSignatureParams(filterableFields);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".service;")
                .blank()
                .line("import " + config.getPackageName() + ".dto.request." + entityName + "RequestDTO;")
                .line("import " + config.getPackageName() + ".dto.response." + entityName + "ResponseDTO;")
                .line("import org.springframework.data.domain.Page;")
                .line("import java.util.List;")
                .line("import java.util.UUID;")
                .blank()
                .line("/**")
                .line(" * Service contract for " + entityName + " operations.")
                .line(" */")
                .line("public interface " + entityName + "Service {")
                .blank()
                .indent(t -> {
                    t.line("/** Create a new " + entityName + " record */");
                    t.line(entityName + "ResponseDTO create(" + entityName + "RequestDTO requestDTO);");
                    t.blank();

                    t.line("/** Update an existing " + entityName + " by UUID */");
                    t.line(entityName + "ResponseDTO update(UUID uuid, " + entityName + "RequestDTO requestDTO);");
                    t.blank();

                    t.line("/** Fetch a single " + entityName + " by UUID */");
                    t.line(entityName + "ResponseDTO getByUuid(UUID uuid);");
                    t.blank();

                    t.line("/** Paginated list with optional search and filters */");
                    t.line("Page<" + entityName + "ResponseDTO> getAll(int page, int size, String search" + getAllParams + ");");
                    t.blank();

                    t.line("/** Soft-delete a " + entityName + " by UUID */");
                    t.line("void delete(UUID uuid);");
                    t.blank();

                    t.line("/** Toggle active/inactive status */");
                    t.line(entityName + "ResponseDTO updateStatus(UUID uuid, String status);");
                    t.blank();

                    if (config.isEnableCount()) {
                        t.line("/** Count all non-deleted " + entityName + " records */");
                        t.line("long countActive();");
                        t.blank();
                    }

                    if (config.isEnableDropdown()) {
                        t.line("/** Lightweight list for dropdown/select components */");
                        t.line("List<" + entityName + "DropdownItem> getDropdown();");
                        t.blank();
                    }

                    return t;
                })
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // Service Implementation
    // ─────────────────────────────────────────────────────────

    public String generateImpl(ModuleConfig config) {
        String entityName = NamingUtil.toEntityName(config);
        log.info("Generating ServiceImpl: {}ServiceImpl", entityName);

        List<FieldConfig> filterableFields = config.getFields().stream()
                .filter(FieldConfig::isFilterable).toList();

        ImportRegistry imports = buildImplImports(config, entityName);

        return CodeTemplate.of()
                .line("package " + config.getPackageName() + ".service.impl;")
                .blank()
                .append(imports.appendTo(CodeTemplate.of()))
                .blank()
                .line("/**")
                .line(" * Service implementation for " + entityName + ".")
                .line(" * Read operations use @Transactional(readOnly=true) for performance.")
                .line(" * Write operations are individually annotated @Transactional.")
                .line(" */")
                .line("@Slf4j")
                .line("@Service")
                .line("@RequiredArgsConstructor")
                .line("@Transactional(readOnly = true)")
                .line("public class " + entityName + "ServiceImpl implements " + entityName + "Service {")
                .blank()
                .indent(t -> t
                        .line("private final " + entityName + "Repository repository;")
                        .line("private final " + entityName + "Mapper mapper;")
                )
                .blank()
                // ── create ────────────────────────────────────────
                .indent(t -> generateCreate(t, config, entityName))
                .blank()
                // ── update ────────────────────────────────────────
                .indent(t -> generateUpdate(t, config, entityName))
                .blank()
                // ── getByUuid ─────────────────────────────────────
                .indent(t -> generateGetByUuid(t, config, entityName))
                .blank()
                // ── getAll ────────────────────────────────────────
                .indent(t -> generateGetAll(t, config, entityName, filterableFields))
                .blank()
                // ── delete ────────────────────────────────────────
                .indent(t -> generateDelete(t, config, entityName))
                .blank()
                // ── updateStatus ──────────────────────────────────
                .indent(t -> generateUpdateStatus(t, config, entityName))
                .blank()
                // ── countActive ───────────────────────────────────
                .when(config.isEnableCount(), t ->
                        t.indent(tt -> generateCountActive(tt, entityName)).blank()
                )
                // ── getDropdown ───────────────────────────────────
                .when(config.isEnableDropdown(), t ->
                        t.indent(tt -> generateGetDropdown(tt, config, entityName)).blank()
                )
                // ── helper: findByUuid ────────────────────────────
                .indent(t -> generateFindByUuidHelper(t, config, entityName))
                .line("}")
                .render();
    }

    // ─────────────────────────────────────────────────────────
    // create()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateCreate(CodeTemplate t, ModuleConfig config, String entityName) {
        String uuidGetter = NamingUtil.getter(config.getUuidField());

        t.line("@Override")
                .line("@Transactional")
                .line("public " + entityName + "ResponseDTO create(" + entityName + "RequestDTO requestDTO) {")
                .indent(inner -> {
                    inner.line("log.info(\"Creating new " + config.getModuleName() + "\");");
                    inner.blank();

                    // Unique constraint checks
                    for (UniqueConstraint uc : config.getUniqueConstraints()) {
                        // Only generate check if all fields are present in requestDTO
                        if (uc.getFields().stream().allMatch(f -> isFieldInRequest(config, f))) {
                            String params = uc.getFields().stream()
                                    .map(f -> "requestDTO." + NamingUtil.getter(f) + "()")
                                    .collect(Collectors.joining(", "));
                            inner.line("if (repository." + uc.existsMethodName() + "(" + params + ")) {");
                            inner.indent(g -> g.line("throw new BusinessException(" + config.resolvedDuplicateCode() + ");"));
                            inner.line("}");
                            inner.blank();
                        } else {
                            log.debug("Skipping unique check {} because some fields are missing from RequestDTO", uc.existsMethodName());
                        }
                    }

                    inner.line(entityName + " entity = mapper.toEntity(requestDTO);");
                    inner.line(entityName + " saved = repository.save(entity);");
                    inner.blank();
                    inner.line("log.info(\"Created " + config.getModuleName() + " with UUID: {}\", saved." + uuidGetter + "());");
                    inner.line("return getByUuid(saved." + uuidGetter + "());");
                    return inner;
                })
                .line("}");
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // update()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUpdate(CodeTemplate t, ModuleConfig config, String entityName) {
        String pkGetter  = NamingUtil.getter(config.getPrimaryKey());
        String uuidGetter = NamingUtil.getter(config.getUuidField());

        t.line("@Override")
                .line("@Transactional")
                .line("public " + entityName + "ResponseDTO update(UUID uuid, " + entityName + "RequestDTO requestDTO) {")
                .indent(inner -> {
                    inner.line("log.info(\"Updating " + config.getModuleName() + " with UUID: {}\", uuid);");
                    inner.blank();
                    inner.line(entityName + " entity = findByUuid(uuid);");
                    inner.blank();

                    // Unique constraint checks (exclude self)
                    for (UniqueConstraint uc : config.getUniqueConstraints()) {
                        // Only generate check if all fields are present in requestDTO
                        if (uc.getFields().stream().allMatch(f -> isFieldInRequest(config, f))) {
                            String params = uc.getFields().stream()
                                    .map(f -> "requestDTO." + NamingUtil.getter(f) + "()")
                                    .collect(Collectors.joining(", "));
                            inner.line("if (repository." + uc.existsNotIdMethodName() +
                                    "(entity." + pkGetter + "(), " + params + ")) {");
                            inner.indent(g -> g.line("throw new BusinessException(" + config.resolvedDuplicateCode() + ");"));
                            inner.line("}");
                            inner.blank();
                        }
                    }

                    inner.line("mapper.updateEntity(entity, requestDTO);");
                    inner.line(entityName + " saved = repository.save(entity);");
                    inner.blank();
                    inner.line("log.info(\"Updated " + config.getModuleName() + " with UUID: {}\", uuid);");
                    inner.line("return getByUuid(saved." + uuidGetter + "());");
                    return inner;
                })
                .line("}");
        return t;
    }

    // ─────────────────────────────────────────────────────────
    // getByUuid()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateGetByUuid(CodeTemplate t, ModuleConfig config, String entityName) {
        boolean cache = config.isEnableCache();
        return t
                .lineIf(cache, "@Cacheable(value = \"" + config.resolvedCacheName() + "\", key = \"#uuid\")")
                .line("@Override")
                .line("public " + entityName + "ResponseDTO getByUuid(UUID uuid) {")
                .indent(inner -> inner
                        .line("log.debug(\"Fetching " + config.getModuleName() + " uuid={}\", uuid);")
                        .line("return repository.findProjectionByUuid(uuid)")
                        .line("        .map(mapper::toResponseDTO)")
                        .line("        .orElseThrow(() -> new BusinessException(" + config.resolvedNotFoundCode() + "));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // getAll()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateGetAll(CodeTemplate t, ModuleConfig config,
                                        String entityName, List<FieldConfig> filterableFields) {
        String extraParams = buildGetAllSignatureParams(filterableFields);
        String repoArgs    = buildGetAllRepoArgs(filterableFields);

        return t
                .line("@Override")
                .line("public Page<" + entityName + "ResponseDTO> getAll(int page, int size, String search" + extraParams + ") {")
                .indent(inner -> inner
                        .line("log.debug(\"Listing " + config.getModuleName() + " page={} size={}\", page, size);")
                        .line("Pageable pageable = PageRequest.of(page, size,")
                        .line("        Sort.by(\"" + config.getDefaultSortField() + "\")." +
                                (config.getDefaultSortDirection().equalsIgnoreCase("ASC") ? "ascending()" : "descending()") + ");")
                        .line("return repository.findAllWithFilters(search" + repoArgs + ", pageable)")
                        .line("                 .map(mapper::toResponseDTO);")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // delete()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateDelete(CodeTemplate t, ModuleConfig config, String entityName) {
        String pkGetter = NamingUtil.getter(config.getPrimaryKey());
        boolean cache   = config.isEnableCache();

        return t
                .lineIf(cache, "@CacheEvict(value = \"" + config.resolvedCacheName() + "\", key = \"#uuid\")")
                .line("@Override")
                .line("@Transactional")
                .line("public void delete(UUID uuid) {")
                .indent(inner -> inner
                        .line("log.info(\"Deleting " + config.getModuleName() + " uuid={}\", uuid);")
                        .line(entityName + " entity = findByUuid(uuid);")
                        .line("repository.softDeleteById(entity." + pkGetter + "(), SecurityUtils.getCurrentUserId());")
                        .line("log.info(\"Deleted " + config.getModuleName() + " uuid={}\", uuid);")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // updateStatus()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateUpdateStatus(CodeTemplate t, ModuleConfig config, String entityName) {
        String uuidGetter = NamingUtil.getter(config.getUuidField());
        boolean cache     = config.isEnableCache();

        return t
                .lineIf(cache, "@CacheEvict(value = \"" + config.resolvedCacheName() + "\", key = \"#uuid\")")
                .line("@Override")
                .line("@Transactional")
                .line("public " + entityName + "ResponseDTO updateStatus(UUID uuid, String status) {")
                .indent(inner -> inner
                        .line("log.info(\"Status update: " + config.getModuleName() + " uuid={} → {}\", uuid, status);")
                        .line(entityName + " entity = findByUuid(uuid);")
                        .line("entity.setStatus(StatusEnum.fromName(status).getCode());")
                        .line(entityName + " saved = repository.save(entity);")
                        .line("return getByUuid(saved." + uuidGetter + "());")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // countActive()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateCountActive(CodeTemplate t, String entityName) {
        return t
                .line("@Override")
                .line("public long countActive() {")
                .indent(inner -> inner.line("return repository.countActive();"))
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // getDropdown()
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateGetDropdown(CodeTemplate t, ModuleConfig config, String entityName) {
        return t
                .line("@Override")
                .line("public List<" + entityName + "DropdownItem> getDropdown() {")
                .indent(inner -> inner
                        .line("return repository.findAllForDropdown().stream()")
                        .line("        .map(p -> new " + entityName + "DropdownItem(p.getValue(), p.getLabel()))")
                        .line("        .toList();")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // findByUuid helper
    // ─────────────────────────────────────────────────────────

    private CodeTemplate generateFindByUuidHelper(CodeTemplate t, ModuleConfig config, String entityName) {
        return t
                .line("/** Internal helper — throws " + config.resolvedNotFoundCode() + " if not found */")
                .line("private " + entityName + " findByUuid(UUID uuid) {")
                .indent(inner -> inner
                        .line("return repository.findByUuid(uuid)")
                        .line("        .orElseThrow(() -> new BusinessException(" + config.resolvedNotFoundCode() + "));")
                )
                .line("}");
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    /** Build extra filter params for getAll signature */
    private String buildGetAllSignatureParams(List<FieldConfig> filterableFields) {
        if (filterableFields.isEmpty()) return "";
        return filterableFields.stream()
                .map(f -> ", String " + f.getName())
                .collect(Collectors.joining());
    }

    /** Build extra repo args for getAll call */
    private String buildGetAllRepoArgs(List<FieldConfig> filterableFields) {
        if (filterableFields.isEmpty()) return "";
        return filterableFields.stream()
                .map(f -> ", " + f.getName())
                .collect(Collectors.joining());
    }

    // ─────────────────────────────────────────────────────────
    // Imports
    // ─────────────────────────────────────────────────────────

    private ImportRegistry buildImplImports(ModuleConfig config, String entityName) {
        String pkg = config.getPackageName();
        return new ImportRegistry()
                .add(pkg + ".dto.projection." + entityName + "Projection")
                .add(pkg + ".dto.request." + entityName + "RequestDTO")
                .add(pkg + ".dto.response." + entityName + "ResponseDTO")
                .add(pkg + ".entity." + entityName)
                .add(pkg + ".mapper." + entityName + "Mapper")
                .add(pkg + ".repository." + entityName + "Repository")
                .add(pkg + ".service." + entityName + "Service")
                .addAll(
                        "com.apiCodeGenerator.common.apputil.exception.custom.BusinessException",
                        "com.apiCodeGenerator.common.apputil.response.ErrorCode",
                        "com.apiCodeGenerator.common.apputil.enums.StatusEnum",
                        "com.apiCodeGenerator.common.apputil.utils.commonutil.SecurityUtils",
                        "lombok.RequiredArgsConstructor",
                        "lombok.extern.slf4j.Slf4j",
                        "org.springframework.data.domain.Page",
                        "org.springframework.data.domain.PageRequest",
                        "org.springframework.data.domain.Pageable",
                        "org.springframework.data.domain.Sort",
                        "org.springframework.stereotype.Service",
                        "org.springframework.transaction.annotation.Transactional",
                        "java.util.List",
                        "java.util.UUID"
                )
                .addIf(config.isEnableCache(),
                        "org.springframework.cache.annotation.Cacheable",
                        "org.springframework.cache.annotation.CacheEvict");
    }

    // Special vararg version
    private ImportRegistry addIf(ImportRegistry reg, boolean condition, String... fqns) {
        if (condition) for (String f : fqns) reg.add(f);
        return reg;
    }
    private boolean isFieldInRequest(ModuleConfig config, String fieldName) {
        return config.getFields().stream()
                .anyMatch(f -> f.getName().equals(fieldName) && !f.isExcludeFromRequest());
    }
}