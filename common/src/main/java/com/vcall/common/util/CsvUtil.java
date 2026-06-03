package com.vcall.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing CSV files
 */
public class CsvUtil {

    private CsvUtil() {}

    /**
     * Parses a CSV file and returns the data as a list of string arrays
     * 
     * @param inputStream the CSV file input stream
     * @return list of rows, where each row is a string array of column values
     * @throws IOException if an I/O error occurs
     */
    public static List<String[]> parseCsv(java.io.InputStream inputStream) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Simple CSV parsing - splits by comma and handles quoted values
                List<String> values = parseCsvLine(line);
                rows.add(values.toArray(new String[0]));
            }
        }
        return rows;
    }

    /**
     * Parses a single CSV line, handling quoted values
     * 
     * @param line the CSV line to parse
     * @return list of column values
     */
    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }
}