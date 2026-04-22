package com.apiCodeGenerator.common.apputil.redis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private static final String PREFIX = "audit:old:";
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    /* ================= SAVE / UPDATE ================= */
    public void saveOrUpdate(String table, UUID recordId, Map<String, Object> data) {
        try {
            if (recordId == null || data == null || data.isEmpty()) {
                log.warn("Cannot save null/empty data for table: {}, recordId: {}", table, recordId);
                return;
            }
            String key = buildKey(table, recordId);
            Cache cache = cacheManager.getCache("dropdownCache");
            if (cache != null) {
                cache.put(key, data);
                log.debug("Saved data to cache: key={}", key);
            }
        } catch (Exception e) {
            log.error("Error saving to cache for table: {}, recordId: {}", table, recordId, e);
        }
    }

    /* ================= GET ================= */
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String table, UUID recordId) {
        try {
            if (recordId == null) {
                return null;
            }
            String key = buildKey(table, recordId);
            Cache cache = cacheManager.getCache("dropdownCache");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    if (value instanceof Map) {
                        return (Map<String, Object>) value;
                    } else if (value != null) {
                        return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {});
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting from cache for table: {}, recordId: {}", table, recordId, e);
            return null;
        }
    }

    /* ================= DELETE ================= */
    public void delete(String table, UUID recordId) {
        try {
            if (recordId == null) {
                return;
            }
            String key = buildKey(table, recordId);
            Cache cache = cacheManager.getCache("dropdownCache");
            if (cache != null) {
                cache.evict(key);
                log.debug("Deleted from cache: key={}", key);
            }
        } catch (Exception e) {
            log.error("Error deleting from cache for table: {}, recordId: {}", table, recordId, e);
        }
    }

    /* ================= EXISTS ================= */
    public boolean exists(String table, UUID recordId) {
        try {
            if (recordId == null) {
                return false;
            }
            String key = buildKey(table, recordId);
            Cache cache = cacheManager.getCache("dropdownCache");
            if (cache != null) {
                return cache.get(key) != null;
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking existence in cache for table: {}, recordId: {}", table, recordId, e);
            return false;
        }
    }

    /* ================= CLEAR ALL ================= */
    public void clearAll() {
        try {
            Cache cache = cacheManager.getCache("dropdownCache");
            if (cache != null) {
                cache.clear();
                log.debug("Cleared all cache");
            }
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }

    /* ================= KEY BUILDER ================= */
    private String buildKey(String table, UUID recordId) {
        return PREFIX + table + ":" + recordId;
    }
}

