package com.apiCodeGenerator.common.apputil.utils.commonutil;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class AuditFieldUtils {

    private static final ConcurrentHashMap<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();

    private AuditFieldUtils() {}

    public static String[] getNonNullFieldNames(Object obj) {
        if (obj == null) {
            return new String[0];
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = FIELD_CACHE.computeIfAbsent(clazz, AuditFieldUtils::getAllFields);

        List<String> nonNullFields = new ArrayList<>();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    nonNullFields.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                log.warn("Could not access field {} on {}", field.getName(), clazz.getSimpleName(), e);
            }
        }
        return nonNullFields.toArray(new String[0]);
    }

    private static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] declared = current.getDeclaredFields();
            for (Field field : declared) {
                // skip static fields if desired
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    fieldList.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fieldList.toArray(new Field[0]);
    }
}
