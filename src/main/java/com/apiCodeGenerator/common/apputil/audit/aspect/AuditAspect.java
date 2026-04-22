package com.apiCodeGenerator.common.apputil.audit.aspect;


import com.apiCodeGenerator.common.apputil.audit.annotation.AuditLog;
import com.apiCodeGenerator.common.apputil.audit.publisher.AuditPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditPublisher auditPublisher;
    private final ObjectMapper   objectMapper;

    private static final Set<String> IGNORED_FIELDS = Set.of(
            "updatedat", "updatedby", "createdat", "createdby",
            "deletedat", "deletedby", "audittrackerid"
    );

    // ─────────────────────────────────────────────────────────────────────

    @Around("@annotation(auditLog)")
    public Object auditMethod(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {

        MethodSignature sig         = (MethodSignature) pjp.getSignature();
        String[]        paramNames  = sig.getParameterNames();
        Object[]        paramValues = pjp.getArgs();
        String          className   = pjp.getTarget().getClass().getSimpleName();
        String          methodName  = sig.getName();

        Map<String, Object> paramMap = buildParamMap(paramNames, paramValues);

        // ── Step 1: Fetch + IMMEDIATELY DEEP-COPY old state ───────────────
        // Serialize to JsonNode RIGHT NOW before pjp.proceed().
        // JPA first-level cache returns the same Entity reference;
        // mapper.updateEntity() mutates it in-place → without a deep copy
        // oldData would silently equal newData → diff = {}.
        JsonNode oldSnapshot = null;
        if (auditLog.captureOldData()) {
            Object oldRaw = fetchEntityState(pjp, auditLog, paramMap);
            if (oldRaw != null) {
                oldSnapshot = objectMapper.valueToTree(oldRaw); // ← frozen copy
            }
        }

        // ── Step 2: Execute ────────────────────────────────────────────────
        short  statusId = 1;
        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable ex) {
            statusId = 0;
            // tracker_id: try from oldSnapshot (entity may not exist yet)
            BigDecimal trackerId = resolveTrackerId(auditLog, paramMap, oldSnapshot, null);
            publishAudit(auditLog, className, methodName, trackerId,
                    oldSnapshot, serializeRequestFallback(auditLog, paramMap), null, statusId);
            throw ex;
        }

        // ── Step 3: Resolve NEW data (re-fetch so shapes match) ───────────
        JsonNode newSnapshot = resolveNewData(auditLog, paramMap, result, pjp);

        // ── Step 4: Resolve tracker_id ────────────────────────────────────
        // Resolution order:
        //  1. oldSnapshot.trackerIdField  (UPDATE / DELETE — always available)
        //  2. newSnapshot.trackerIdField  (CREATE — entity just saved)
        //  3. trackerParam if it's a Number
        BigDecimal trackerId = resolveTrackerId(auditLog, paramMap, oldSnapshot, newSnapshot);

        // ── Step 5: Diff ───────────────────────────────────────────────────
        List<String> changedFields = null;
        if (oldSnapshot != null && newSnapshot != null) {
            changedFields = diffFields(oldSnapshot, newSnapshot);
        }

        // ── Step 6: Publish ────────────────────────────────────────────────
        publishAudit(auditLog, className, methodName, trackerId,
                oldSnapshot, newSnapshot, changedFields, statusId);

        return result;
    }

    // ═════════════════════════════════════════════════════════════════════
    // tracker_id RESOLUTION
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Resolves the numeric tracker_id to store in sys_audit_log.tracker_id.
     *
     * Priority:
     *  1. trackerIdField on oldSnapshot  → UPDATE / DELETE (entity pre-existed)
     *  2. trackerIdField on newSnapshot  → CREATE (entity just saved, ID now available)
     *  3. trackerParam value if it is a Number  → direct numeric-id shortcut
     */
    private BigDecimal resolveTrackerId(
            AuditLog   auditLog,
            Map<String, Object> paramMap,
            JsonNode   oldSnapshot,
            JsonNode   newSnapshot
    ) {
        String fieldName = auditLog.trackerIdField();

        if (!fieldName.isBlank()) {
            // 1. Try oldSnapshot first (UPDATE / DELETE)
            BigDecimal fromOld = extractNumericField(oldSnapshot, fieldName);
            if (fromOld != null) return fromOld;

            // 2. Try newSnapshot (CREATE — response DTO has the generated PK)
            BigDecimal fromNew = extractNumericField(newSnapshot, fieldName);
            if (fromNew != null) return fromNew;
        }

        // 3. Fallback: trackerParam value if numeric
        if (!auditLog.trackerParam().isBlank()) {
            Object raw = paramMap.get(auditLog.trackerParam());
            if (raw instanceof Number num) {
                return BigDecimal.valueOf(num.longValue());
            }
        }

        return null;
    }

    /**
     * Reads a named field from a JsonNode and converts it to BigDecimal.
     * Returns null if the node is null, the field is absent, or the value
     * is not numeric.
     */
    private BigDecimal extractNumericField(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) return null;
        try {
            return new BigDecimal(fieldNode.asText());
        } catch (NumberFormatException ex) {
            log.debug("AuditAspect: field '{}' value '{}' is not numeric",
                    fieldName, fieldNode.asText());
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // NEW DATA RESOLUTION
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Resolves new_data and serializes it to JsonNode immediately.
     *
     * Priority:
     *  1. fetchMethod present (UPDATE / DELETE) → re-fetch AFTER execution
     *     so oldData and newData are the same DTO type → accurate diff
     *  2. captureResponse → serialize the method return value
     *  3. captureRequest  → serialize filtered param map (CREATE / READ)
     */
    private JsonNode resolveNewData(
            AuditLog            auditLog,
            Map<String, Object> paramMap,
            Object              result,
            ProceedingJoinPoint pjp
    ) {
        // Re-fetch via fetchMethod (ensures same shape as oldSnapshot)
        if (auditLog.captureOldData()
                && !auditLog.fetchMethod().isBlank()
                && !auditLog.trackerParam().isBlank()) {

            Object refetched = fetchEntityState(pjp, auditLog, paramMap);
            if (refetched != null) return objectMapper.valueToTree(refetched);
            return null; // entity was hard-deleted
        }

        // Return value (CREATE with captureResponse, or explicit response capture)
        if (auditLog.captureResponse() && result != null) {
            return objectMapper.valueToTree(result);
        }

        // Raw request args (CREATE default, or plain READ)
        if (auditLog.captureRequest()) {
            Object data = buildRequestData(auditLog, paramMap);
            return data != null ? objectMapper.valueToTree(data) : null;
        }

        return null;
    }

    // ═════════════════════════════════════════════════════════════════════
    // ENTITY STATE FETCH
    // ═════════════════════════════════════════════════════════════════════

    /**
     * Calls fetchMethod on the raw bean target (NOT the Spring proxy).
     * Bypasses all AOP interceptors → no nested @AuditLog fires.
     */
    private Object fetchEntityState(
            ProceedingJoinPoint pjp,
            AuditLog            auditLog,
            Map<String, Object> paramMap
    ) {
        if (auditLog.fetchMethod().isBlank() || auditLog.trackerParam().isBlank()) return null;

        Object identifier = paramMap.get(auditLog.trackerParam());
        if (identifier == null) return null;

        try {
            Object target     = pjp.getTarget(); // raw bean — bypasses AOP
            Method fetchMethod = findMethod(target.getClass(),
                    auditLog.fetchMethod(),
                    identifier.getClass());
            if (fetchMethod == null) {
                log.warn("AuditAspect: fetchMethod '{}({})' not found on {}",
                        auditLog.fetchMethod(), identifier.getClass().getSimpleName(),
                        target.getClass().getSimpleName());
                return null;
            }
            fetchMethod.setAccessible(true);
            return fetchMethod.invoke(target, identifier);
        } catch (Exception ex) {
            log.error("AuditAspect: fetchEntityState failed via '{}' — {}",
                    auditLog.fetchMethod(), ex.getMessage(), ex);
            return null;
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?> paramType) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(methodName))                       continue;
                if (m.getParameterCount() != 1)                            continue;
                if (m.getParameterTypes()[0].isAssignableFrom(paramType)) return m;
            }
        }
        return null;
    }

    // ═════════════════════════════════════════════════════════════════════
    // REQUEST DATA
    // ═════════════════════════════════════════════════════════════════════

    private Object buildRequestData(AuditLog auditLog, Map<String, Object> paramMap) {
        if (!auditLog.captureRequest() || paramMap.isEmpty()) return null;
        Map<String, Object> filtered = new LinkedHashMap<>();
        paramMap.forEach((name, value) -> {
            if (value instanceof jakarta.servlet.http.HttpServletRequest)  return;
            if (value instanceof jakarta.servlet.http.HttpServletResponse) return;
            filtered.put(name, value);
        });
        return filtered.isEmpty() ? null : filtered;
    }

    private JsonNode serializeRequestFallback(AuditLog auditLog, Map<String, Object> paramMap) {
        Object data = buildRequestData(auditLog, paramMap);
        return data != null ? objectMapper.valueToTree(data) : null;
    }

    // ═════════════════════════════════════════════════════════════════════
    // DIFF
    // ═════════════════════════════════════════════════════════════════════

    private List<String> diffFields(JsonNode oldNode, JsonNode newNode) {
        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldNode, Map.class);
            Map<String, Object> newMap = objectMapper.convertValue(newNode, Map.class);
            List<String> changed = new ArrayList<>();
            compareMaps("", oldMap, newMap, changed);
            return changed;
        } catch (Exception ex) {
            log.debug("AuditAspect: diff failed — {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private void compareMaps(String prefix, Map<String, Object> oldMap,
                             Map<String, Object> newMap, List<String> changed) {
        Set<String> allKeys = new LinkedHashSet<>();
        if (oldMap != null) allKeys.addAll(oldMap.keySet());
        if (newMap != null) allKeys.addAll(newMap.keySet());

        for (String key : allKeys) {
            if (IGNORED_FIELDS.contains(key.toLowerCase())) continue;

            Object oldVal    = oldMap != null ? oldMap.get(key) : null;
            Object newVal    = newMap != null ? newMap.get(key) : null;
            String fieldPath = prefix.isEmpty() ? key : prefix + "." + key;

            if (oldVal instanceof Map && newVal instanceof Map) {
                compareMaps(fieldPath, (Map<String, Object>) oldVal,
                        (Map<String, Object>) newVal, changed);
                continue;
            }
            if (oldVal instanceof List && newVal instanceof List) {
                if (!Objects.equals(oldVal, newVal)) changed.add(fieldPath);
                continue;
            }
            if (oldVal instanceof Number && newVal instanceof Number) {
                try {
                    if (new BigDecimal(oldVal.toString())
                            .compareTo(new BigDecimal(newVal.toString())) != 0) {
                        changed.add(fieldPath);
                    }
                } catch (NumberFormatException e) {
                    if (!Objects.equals(oldVal.toString(), newVal.toString()))
                        changed.add(fieldPath);
                }
                continue;
            }
            if (!Objects.equals(oldVal, newVal)) changed.add(fieldPath);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // PUBLISH & UTILS
    // ═════════════════════════════════════════════════════════════════════

    private void publishAudit(AuditLog auditLog, String className, String methodName,
                              BigDecimal trackerId, Object oldData, Object newData,
                              List<String> changedFields, short statusId) {
        try {
            String funcName = auditLog.description().isBlank() ? methodName : auditLog.description();
            auditPublisher.publish(
                    (short) auditLog.action().getActionId(),
                    funcName, className,
                    (short) auditLog.menuId(),
                    trackerId,
                    oldData, newData,
                    changedFields, statusId
            );
        } catch (Exception ex) {
            log.error("AuditAspect: failed to publish — {}", ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildParamMap(String[] names, Object[] values) {
        if (values == null || values.length == 0) return Collections.emptyMap();
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            String key = (names != null && i < names.length) ? names[i] : "arg" + i;
            map.put(key, values[i]);
        }
        return map;
    }
}