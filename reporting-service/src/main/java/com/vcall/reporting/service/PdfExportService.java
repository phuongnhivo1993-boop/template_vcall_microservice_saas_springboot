package com.vcall.reporting.service;

import com.vcall.reporting.entity.ReportExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    public Resource exportToPdf(ReportExecution execution, Map<String, Object> data) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                PDType0Font font = new PDType0Font(document, new Standard14Fonts(Standard14Fonts.FontName.HELVETICA));
                PDType0Font fontBold = new PDType0Font(document, new Standard14Fonts(Standard14Fonts.FontName.HELVETICA_BOLD));

                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float yPosition = yStart;
                int fontSize = 10;
                int headerSize = 16;

                cs.beginText();
                cs.setFont(fontBold, headerSize);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("VCall Report: " + execution.getReportDefinition().getName());
                cs.endText();
                yPosition -= 30;

                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cs.endText();
                yPosition -= 20;

                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("Status: " + execution.getStatus().name());
                cs.endText();
                yPosition -= 20;

                if (execution.getExecutionTime() != null) {
                    cs.beginText();
                    cs.setFont(font, fontSize);
                    cs.newLineAtOffset(margin, yPosition);
                    cs.showText("Execution time: " + execution.getExecutionTime() + " ms");
                    cs.endText();
                    yPosition -= 20;
                }

                yPosition -= 10;
                cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                cs.moveTo(margin, yPosition);
                cs.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                cs.stroke();
                yPosition -= 20;

                if (data != null) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        if (yPosition < 80) {
                            cs.endText();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            cs.close();
                            PDPageContentStream csNew = new PDPageContentStream(document, page);
                            cs = csNew;
                            yPosition = yStart;
                            cs.beginText();
                            cs.setFont(font, fontSize);
                            cs.newLineAtOffset(margin, yPosition);
                        }

                        cs.beginText();
                        cs.setFont(fontBold, fontSize);
                        cs.newLineAtOffset(margin, yPosition);
                        cs.showText(entry.getKey() + ":");
                        cs.endText();
                        yPosition -= 16;

                        cs.beginText();
                        cs.setFont(font, fontSize);
                        cs.newLineAtOffset(margin + 10, yPosition);
                        cs.showText(String.valueOf(entry.getValue()));
                        cs.endText();
                        yPosition -= 22;
                    }
                }

                yPosition -= 20;
                cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                cs.moveTo(margin, yPosition);
                cs.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                cs.stroke();
                yPosition -= 20;

                cs.beginText();
                cs.setFont(font, 8);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("VCall Contact Center - Confidential");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            log.error("Failed to generate PDF for execution {}: {}", execution.getId(), e.getMessage());
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}
