package com.vcall.common.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExcelImportUtil {

    private ExcelImportUtil() {}

    public static List<String[]> parseXlsx(InputStream is) throws Exception {
        Map<Integer, String> sharedStrings = new HashMap<>();
        List<String[]> rows = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                byte[] content = readEntryBytes(zis);
                if (entry.getName().equals("xl/sharedStrings.xml")) {
                    Document doc = builder.parse(new ByteArrayInputStream(content));
                    NodeList siNodes = doc.getElementsByTagName("si");
                    for (int i = 0; i < siNodes.getLength(); i++) {
                        sharedStrings.put(i, siNodes.item(i).getTextContent());
                    }
                } else if (entry.getName().equals("xl/worksheets/sheet1.xml")) {
                    Document doc = builder.parse(new ByteArrayInputStream(content));
                    NodeList rowNodes = doc.getElementsByTagName("row");
                    for (int i = 0; i < rowNodes.getLength(); i++) {
                        Element rowElement = (Element) rowNodes.item(i);
                        NodeList cellNodes = rowElement.getElementsByTagName("c");
                        String[] rowData = new String[cellNodes.getLength()];
                        for (int j = 0; j < cellNodes.getLength(); j++) {
                            Element cell = (Element) cellNodes.item(j);
                            String cellType = cell.getAttribute("t");
                            String value = "";
                            NodeList vNodes = cell.getElementsByTagName("v");
                            if (vNodes.getLength() > 0) {
                                value = vNodes.item(0).getTextContent();
                            }
                            if ("s".equals(cellType) && !value.isEmpty()) {
                                int idx = Integer.parseInt(value);
                                value = sharedStrings.getOrDefault(idx, "");
                            }
                            rowData[j] = value;
                        }
                        rows.add(rowData);
                    }
                }
            }
        }
        return rows;
    }

    private static byte[] readEntryBytes(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }
}
