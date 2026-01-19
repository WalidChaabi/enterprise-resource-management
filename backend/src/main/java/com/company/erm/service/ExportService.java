package com.company.erm.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.SearchEntityType;
import com.company.erm.dto.SearchItemDTO;
import com.company.erm.dto.SearchResultDTO;

/**
 * Service for exporting search results to CSV and PDF formats.
 */
@Service
@Transactional(readOnly = true)
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export search results to CSV format.
     * 
     * @param searchResult Search results to export
     * @return CSV content as byte array
     */
    public byte[] exportToCSV(SearchResultDTO searchResult) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        
        // Add BOM for Excel compatibility
        writer.write('\ufeff');
        
        CSVFormat csvFormat = CSVFormat.Builder.create()
            .setHeader("Entity Type", "ID", "Title", "Description", "Date", "Created At", "Additional Info")
            .setQuote('"')
            .setEscape('\\')
            .build();
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            // Write header row with search query info
            csvPrinter.printComment("Search Query: " + searchResult.getQuery());
            csvPrinter.printComment("Total Results: " + searchResult.getTotalResults());
            csvPrinter.printComment("Export Date: " + LocalDate.now().format(DATE_FORMATTER));
            csvPrinter.println();
            
            // Write results grouped by entity type
            for (Map.Entry<SearchEntityType, SearchResultDTO.EntitySearchResults> entry : 
                 searchResult.getResultsByType().entrySet()) {
                
                SearchEntityType entityType = entry.getKey();
                SearchResultDTO.EntitySearchResults entityResults = entry.getValue();
                
                // Write entity type header
                csvPrinter.printComment("=== " + entityType.name() + " (" + entityResults.getCount() + " results) ===");
                csvPrinter.println();
                
                // Write data rows
                for (SearchItemDTO item : entityResults.getItems()) {
                    csvPrinter.print(entityType.name());
                    csvPrinter.print(item.getId().toString());
                    csvPrinter.print(item.getTitle() != null ? item.getTitle() : "");
                    csvPrinter.print(item.getDescription() != null ? item.getDescription() : "");
                    csvPrinter.print(item.getDate() != null ? item.getDate().format(DATE_FORMATTER) : "");
                    csvPrinter.print(item.getCreatedAt() != null 
                        ? item.getCreatedAt().format(DATETIME_FORMATTER) : "");
                    
                    // Format metadata as key-value pairs
                    String metadata = formatMetadata(item.getMetadata());
                    csvPrinter.print(metadata);
                    csvPrinter.println();
                }
                
                csvPrinter.println();
            }
        }
        
        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * Export search results to PDF format.
     * 
     * @param searchResult Search results to export
     * @return PDF content as byte array
     */
    public byte[] exportToPDF(SearchResultDTO searchResult) throws IOException {
        try (PDDocument document = new PDDocument()) {
            float margin = 50;
            float lineHeight = 14;
            float fontSize = 12;
            float titleFontSize = 16;
            
            PDType1Font font = PDType1Font.HELVETICA;
            PDType1Font boldFont = PDType1Font.HELVETICA_BOLD;
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            float yPosition = page.getMediaBox().getHeight() - margin;
            
            try {
                // Title
                contentStream.beginText();
                contentStream.setFont(boldFont, titleFontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Search Results Export");
                contentStream.endText();
                yPosition -= lineHeight * 2;
                
                // Search query info
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Query: " + searchResult.getQuery());
                contentStream.endText();
                yPosition -= lineHeight;
                
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Total Results: " + searchResult.getTotalResults());
                contentStream.endText();
                yPosition -= lineHeight;
                
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Export Date: " + LocalDate.now().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= lineHeight * 2;
                
                // Results by entity type
                for (Map.Entry<SearchEntityType, SearchResultDTO.EntitySearchResults> entry : 
                     searchResult.getResultsByType().entrySet()) {
                    
                    SearchEntityType entityType = entry.getKey();
                    SearchResultDTO.EntitySearchResults entityResults = entry.getValue();
                    
                    // Check if we need a new page
                    if (yPosition < margin + 100) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                    }
                    
                    // Entity type header
                    contentStream.beginText();
                    contentStream.setFont(boldFont, fontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(entityType.name() + " (" + entityResults.getCount() + " results)");
                    contentStream.endText();
                    yPosition -= lineHeight * 1.5f;
                    
                    // Items
                    for (SearchItemDTO item : entityResults.getItems()) {
                        // Check if we need a new page before adding item
                        if (yPosition < margin + 100) {
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }
                        
                        // Title
                        String title = truncate(item.getTitle(), 80);
                        contentStream.beginText();
                        contentStream.setFont(boldFont, fontSize - 2);
                        contentStream.newLineAtOffset(margin + 10, yPosition);
                        contentStream.showText(title);
                        contentStream.endText();
                        yPosition -= lineHeight;
                        
                        // Description
                        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                            if (yPosition < margin + 50) {
                                contentStream.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                contentStream = new PDPageContentStream(document, page);
                                yPosition = page.getMediaBox().getHeight() - margin;
                            }
                            String description = truncate(item.getDescription(), 100);
                            contentStream.beginText();
                            contentStream.setFont(font, fontSize - 2);
                            contentStream.newLineAtOffset(margin + 10, yPosition);
                            contentStream.showText(description);
                            contentStream.endText();
                            yPosition -= lineHeight;
                        }
                        
                        // Metadata
                        if (item.getMetadata() != null && !item.getMetadata().isEmpty()) {
                            if (yPosition < margin + 50) {
                                contentStream.close();
                                page = new PDPage(PDRectangle.A4);
                                document.addPage(page);
                                contentStream = new PDPageContentStream(document, page);
                                yPosition = page.getMediaBox().getHeight() - margin;
                            }
                            String metadata = truncate(formatMetadata(item.getMetadata()), 100);
                            contentStream.beginText();
                            contentStream.setFont(font, fontSize - 3);
                            contentStream.newLineAtOffset(margin + 10, yPosition);
                            contentStream.showText(metadata);
                            contentStream.endText();
                            yPosition -= lineHeight;
                        }
                        
                        yPosition -= lineHeight * 0.5f;
                    }
                    
                    yPosition -= lineHeight;
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String formatMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!first) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
