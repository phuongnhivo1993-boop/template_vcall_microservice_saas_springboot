package com.vcall.common.gdpr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vcall.common.validation.PII;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Service
public class GdprService {

    private final ObjectMapper objectMapper;

    public GdprService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Map<String, Object> exportUserData(Object userEntity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exportDate", new Date());
        data.put("entityType", userEntity.getClass().getSimpleName());

        Map<String, Object> piiData = new LinkedHashMap<>();
        Map<String, Object> nonPiiData = new LinkedHashMap<>();

        for (Field field : getAllFields(userEntity.getClass())) {
            field.setAccessible(true);
            try {
                Object value = field.get(userEntity);
                if (field.isAnnotationPresent(PII.class)) {
                    PII annotation = field.getAnnotation(PII.class);
                    Map<String, Object> piiEntry = new LinkedHashMap<>();
                    piiEntry.put("value", value);
                    piiEntry.put("description", annotation.description());
                    piiEntry.put("sensitive", annotation.sensitive());
                    piiData.put(field.getName(), piiEntry);
                } else {
                    nonPiiData.put(field.getName(), value);
                }
            } catch (Exception e) {
                log.trace("Could not access field {}: {}", field.getName(), e.getMessage());
            }
        }

        data.put("piiData", piiData);
        data.put("nonPiiData", nonPiiData);
        return data;
    }

    public String exportUserDataAsJson(Object userEntity) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(exportUserData(userEntity));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize GDPR data: {}", e.getMessage());
            return "{}";
        }
    }

    public void anonymizePiiFields(Object entity) {
        for (Field field : getAllFields(entity.getClass())) {
            if (field.isAnnotationPresent(PII.class)) {
                field.setAccessible(true);
                try {
                    Class<?> type = field.getType();
                    if (type == String.class) {
                        field.set(entity, "[REDACTED]");
                    } else if (type == Boolean.class || type == boolean.class) {
                        field.set(entity, false);
                    } else if (Number.class.isAssignableFrom(type)) {
                        field.set(entity, 0);
                    } else {
                        field.set(entity, null);
                    }
                } catch (Exception e) {
                    log.warn("Could not anonymize field {}: {}", field.getName(), e.getMessage());
                }
            }
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}
