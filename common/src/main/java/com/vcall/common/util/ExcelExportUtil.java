package com.vcall.common.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExcelExportUtil() {}

    public static <T> void writeExcel(HttpServletResponse response, String fileName,
                                       List<String> headers, Collection<T> items,
                                       List<String> fieldNames) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        List<List<String>> rows = toRows(items, fieldNames);
        byte[] excelBytes = generateXlsx(headers, rows);
        response.setContentLength(excelBytes.length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(excelBytes);
            os.flush();
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
            if (val == null) return "";
            if (val instanceof LocalDateTime) return ((LocalDateTime) val).format(DATETIME_FMT);
            if (val instanceof LocalDate) return ((LocalDate) val).format(DATE_FMT);
            return val.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static byte[] generateXlsx(List<String> headers, List<List<String>> rows) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>");
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
        sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
        sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"");
        sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
        sb.append("<Worksheet ss:Name=\"Sheet1\"><Table>");

        sb.append("<Row>");
        for (String header : headers) {
            sb.append("<Cell><Data ss:Type=\"String\">");
            sb.append(escapeXml(header));
            sb.append("</Data></Cell>");
        }
        sb.append("</Row>");

        for (List<String> row : rows) {
            sb.append("<Row>");
            for (String cell : row) {
                sb.append("<Cell><Data ss:Type=\"String\">");
                sb.append(escapeXml(cell == null ? "" : cell));
                sb.append("</Data></Cell>");
            }
            sb.append("</Row>");
        }

        sb.append("</Table></Worksheet></Workbook>");
        return sb.toString().getBytes("UTF-8");
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
