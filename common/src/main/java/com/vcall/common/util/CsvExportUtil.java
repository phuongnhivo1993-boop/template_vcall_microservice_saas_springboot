package com.vcall.common.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CsvExportUtil {

    private CsvExportUtil() {}

    public static void writeCsv(HttpServletResponse response, String fileName, List<String> headers,
                                 Collection<List<String>> rows) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        try (PrintWriter writer = response.getWriter()) {
            writer.println(headers.stream().map(h -> escapeCsv(h)).collect(Collectors.joining(",")));
            for (List<String> row : rows) {
                writer.println(row.stream().map(CsvExportUtil::escapeCsv).collect(Collectors.joining(",")));
            }
            writer.flush();
        }
    }

    public static <T> List<List<String>> toRows(Collection<T> items, List<String> fieldNames) {
        return items.stream().map(item -> fieldNames.stream()
                .map(f -> getFieldValue(item, f))
                .collect(Collectors.toList())).collect(Collectors.toList());
    }

    private static String getFieldValue(Object obj, String fieldName) {
        try {
            Field field = getAllFields(obj.getClass()).stream()
                    .filter(f -> f.getName().equals(fieldName))
                    .findFirst().orElse(null);
            if (field == null) return "";
            field.setAccessible(true);
            Object val = field.get(obj);
            return val == null ? "" : val.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new java.util.ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
