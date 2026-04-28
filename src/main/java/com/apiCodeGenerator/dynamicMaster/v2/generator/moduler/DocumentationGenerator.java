package com.apiCodeGenerator.dynamicMaster.v2.generator.moduler;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.*;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * DocumentationGenerator — High Quality Word Document Generator
 * ============================================================
 * Produces a premium API documentation with:
 *  - Custom color palette & professional borders
 *  - Syntax-highlighted JSON blocks
 *  - Hierarchical numbering (1.1, 1.2)
 *  - REST method badges (GET, POST, etc.)
 */
@Slf4j
@Component
public class DocumentationGenerator {

    // ─── Color Palette (Matching JS Reference) ─────────────────
    private static final String PRIMARY      = "1B4F72"; // Dark Blue
    private static final String ACCENT       = "2E86C1"; // Med Blue
    private static final String ACCENT_LIGHT = "D6EAF8"; // Light Blue
    private static final String SUCCESS_BG   = "D5F5E3"; // Green BG
    private static final String SUCCESS_TEXT = "1A7A4A";
    private static final String WARNING_BG   = "FDEBD0"; // Orange BG
    private static final String WARNING_TEXT = "935116";
    private static final String DANGER_BG    = "FADBD8"; // Red BG
    private static final String DANGER_TEXT  = "922B21";
    private static final String PURPLE_BG    = "F4ECF7";
    private static final String PURPLE_TEXT  = "7D3C98";
    private static final String ROW_ALT      = "EBF5FB";
    private static final String BORDER_COLOR = "AED6F1";
    private static final String JSON_BG      = "1E2834";
    private static final String JSON_KEY     = "C0392B"; // Red
    private static final String JSON_STR     = "1A7A4A"; // Green
    private static final String JSON_NUM     = "1F618D"; // Blue
    private static final String JSON_PUNCT   = "BDC3C7"; // Gray

    public byte[] generate(ModuleConfig config) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String entityName = NamingUtil.toEntityName(config);

            // ── 1. Cover Page ──────────────────────────────────────
            createCoverPage(document, config, entityName);

            // ── 2. Global Standards ─────────────────────────────────
            addSectionTitle(document, "1. Global Standards & Conventions");
            addParagraph(document, "All API interactions for the " + entityName + " module follow these global standards to ensure consistency and reliability.");
            
            addSubTitle(document, "1.1 General Communication Rules");
            addBullet(document, "UUID Usage: All entity identifiers in the API are UUIDs. Internal integer IDs are never exposed.");
            addBullet(document, "Naming Convention: JSON keys use camelCase (e.g., " + NamingUtil.decapitalize(entityName) + "Uuid).");
            addBullet(document, "Soft Delete: Physical deletion is disabled. Records use status = 'deleted' (internal value 9).");
            
            addSubTitle(document, "1.2 HTTP Status Codes");
            createHttpStatusTable(document);
            addSpacer(document);

            // ── 3. Module Schema ───────────────────────────────────
            addPageBreak(document);
            addSectionTitle(document, "2. " + entityName + " Data Model");
            addParagraph(document, "The following table describes the persistent fields and database mapping for the " + entityName + " module.");
            
            addSubTitle(document, "2.1 Database Table Info");
            createBadgeTable(document, "DB Table:", config.getTableName(), ACCENT);
            
            addSubTitle(document, "2.2 Field Definitions");
            createFieldsTable(document, config);
            addSpacer(document);

            // ── 4. API Endpoints ───────────────────────────────────
            addPageBreak(document);
            addSectionTitle(document, "3. API Reference Documentation");
            addParagraph(document, "Base Path: /api/v1/" + NamingUtil.toUrlPath(config.getModuleName()));

            createEndpointsSection(document, config, entityName);

            // ── 5. Relationships & Validations ─────────────────────
            if (!config.getRelationships().isEmpty()) {
                addPageBreak(document);
                addSectionTitle(document, "4. Relationships & Constraints");
                addSubTitle(document, "4.1 JPA Relationships");
                createRelationshipsTable(document, config);
            }

            document.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate documentation for module {}: {}", config.getModuleName(), e.getMessage(), e);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────
    // Title & Formatting Helpers
    // ─────────────────────────────────────────────────────────

    private void createCoverPage(XWPFDocument document, ModuleConfig config, String entityName) {
        addSpacer(document, 1000);
        
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(36);
        titleRun.setColor(PRIMARY);
        titleRun.setText(entityName.toUpperCase() + " MODULE");
        
        XWPFParagraph subTitlePara = document.createParagraph();
        subTitlePara.setAlignment(ParagraphAlignment.CENTER);
        subTitlePara.setBorderBottom(Borders.SINGLE);
        XWPFRun subTitleRun = subTitlePara.createRun();
        subTitleRun.setFontSize(22);
        subTitleRun.setColor(ACCENT);
        subTitleRun.setText("API Reference Documentation");
        
        addSpacer(document, 200);
        
        XWPFParagraph infoPara = document.createParagraph();
        infoPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun infoRun = infoPara.createRun();
        infoRun.setItalic(true);
        infoRun.setText("Pharma Platform  |  Backend API Standards");
        infoRun.addBreak();
        infoRun.setText("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));

        addSpacer(document, 600);
        
        XWPFTable infoTable = document.createTable(4, 2);
        infoTable.setWidth("80%");
        setTableAlignment(infoTable, STJcTable.CENTER);
        
        fillInfoRow(infoTable.getRow(0), "Base URL", "/api/v1/" + NamingUtil.toUrlPath(config.getModuleName()), false);
        fillInfoRow(infoTable.getRow(1), "Package", config.getPackageName(), true);
        fillInfoRow(infoTable.getRow(2), "Table Name", config.getTableName(), false);
        fillInfoRow(infoTable.getRow(3), "Primary Key", config.getPrimaryKey(), true);

        addPageBreak(document);
    }

    private void addSectionTitle(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBefore(400);
        para.setSpacingAfter(200);
        
        // Border left like the JS code
        CTP ctp = para.getCTP();
        CTPPr ppr = ctp.isSetPPr() ? ctp.getPPr() : ctp.addNewPPr();
        CTPBdr borders = ppr.isSetPBdr() ? ppr.getPBdr() : ppr.addNewPBdr();
        
        CTBorder left = borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft();
        left.setVal(STBorder.SINGLE);
        left.setSz(BigInteger.valueOf(24));
        left.setColor(PRIMARY);
        left.setSpace(BigInteger.valueOf(8));

        CTBorder bottom = borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
        bottom.setVal(STBorder.SINGLE);
        bottom.setSz(BigInteger.valueOf(6));
        bottom.setColor(ACCENT);
        bottom.setSpace(BigInteger.valueOf(4));

        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setFontSize(22);
        run.setColor(PRIMARY);
        run.setText(text);
    }

    private void addSubTitle(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBefore(300);
        para.setSpacingAfter(100);
        
        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setFontSize(16);
        run.setColor(ACCENT);
        run.setText(text);
        
        para.setBorderBottom(Borders.SINGLE);
    }

    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setFontSize(11);
        run.setText(text);
    }

    private void addBullet(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setIndentationLeft(400);
        XWPFRun run = para.createRun();
        run.setText("•  " + text);
        run.setFontSize(11);
    }

    private void addSpacer(XWPFDocument document) {
        addSpacer(document, 100);
    }

    private void addSpacer(XWPFDocument document, int space) {
        XWPFParagraph para = document.createParagraph();
        para.setSpacingBefore(space);
    }

    private void addPageBreak(XWPFDocument document) {
        XWPFParagraph para = document.createParagraph();
        para.createRun().addBreak(BreakType.PAGE);
    }

    // ─────────────────────────────────────────────────────────
    // Table Helpers
    // ─────────────────────────────────────────────────────────

    private void createHttpStatusTable(XWPFDocument document) {
        XWPFTable table = document.createTable(4, 3);
        table.setWidth("100%");
        
        String[] headers = {"Code", "Meaning", "Usage"};
        fillHeaderRow(table.getRow(0), headers);
        
        fillStatusRow(table.getRow(1), "200 OK", "Success", "Successful GET, PUT, PATCH", false);
        fillStatusRow(table.getRow(2), "201 Created", "Resource Created", "Successful POST", true);
        fillStatusRow(table.getRow(3), "400 Bad Request", "Validation Error", "Missing required fields", false);
    }

    private void createFieldsTable(XWPFDocument document, ModuleConfig config) {
        List<FieldConfig> fields = config.getFields();
        XWPFTable table = document.createTable(fields.size() + 1, 4);
        table.setWidth("100%");
        
        fillHeaderRow(table.getRow(0), new String[]{"Field Name", "Type", "Nullable", "Description"});
        
        for (int i = 0; i < fields.size(); i++) {
            FieldConfig f = fields.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            boolean isAlt = i % 2 != 0;
            String fill = isAlt ? ROW_ALT : "FFFFFF";
            
            fillCell(row.getCell(0), f.getName(), true, PRIMARY, fill);
            fillCell(row.getCell(1), f.getType(), false, null, fill);
            fillCell(row.getCell(2), String.valueOf(f.isNullable()), false, null, fill);
            fillCell(row.getCell(3), f.getName() + " field", false, null, fill);
        }
    }

    private void createEndpointsSection(XWPFDocument document, ModuleConfig config, String entityName) {
        String baseUrl = "/api/v1/" + NamingUtil.toUrlPath(config.getModuleName());
        
        // ── 3.1 Create (POST) ───────────────────────────
        addSubTitle(document, "3.1 Create " + entityName);
        createEndpointHeader(document, "POST", baseUrl, "Admin, User");
        addParagraph(document, "Creates a new " + entityName + " record. All validation rules must pass.");
        
        addSubSubTitle(document, "Request Payload:");
        createJsonBlock(document, generateExampleJson(config, true));
        
        addSubSubTitle(document, "Success Response (201 Created):");
        createJsonBlock(document, generateExampleJson(config, false));
        
        // ── 3.2 Get All (GET) ───────────────────────────
        addSpacer(document, 300);
        addSubTitle(document, "3.2 List All / Search");
        createEndpointHeader(document, "GET", baseUrl, "Any");
        addParagraph(document, "Retrieves a paginated list of " + entityName + " records. Supports global search via 'search' query param.");
        
        addSubSubTitle(document, "Response Data Array:");
        createJsonBlock(document, "[\n  " + generateExampleJson(config, false).replace("\n", "\n  ") + "\n]");

        // ── 3.3 Get by UUID (GET) ───────────────────────────
        addSpacer(document, 300);
        addSubTitle(document, "3.3 Get Detail by UUID");
        createEndpointHeader(document, "GET", baseUrl + "/{uuid}", "Any");
        
        addSubSubTitle(document, "3.4 Update Record");
        createEndpointHeader(document, "PUT", baseUrl + "/{uuid}", "Admin");
        addParagraph(document, "Performs a full update of the " + entityName + " record. Missing fields may be overwritten with null.");

        // ── 3.5 Delete (DELETE) ───────────────────────────
        addSpacer(document, 300);
        addSubTitle(document, "3.5 Soft Delete");
        createEndpointHeader(document, "DELETE", baseUrl + "/{uuid}", "Admin");
        addParagraph(document, "Sets the record status to 'deleted'. No physical row is removed.");

        // ── 3.6 Dropdown (GET) ───────────────────────────
        if (config.isEnableDropdown()) {
            addSpacer(document, 300);
            addSubTitle(document, "3.6 Dropdown List");
            createEndpointHeader(document, "GET", baseUrl + "/dropdown", "Any");
            addParagraph(document, "Returns a simplified list of " + entityName + " for UI dropdown components.");
        }
    }

    private void createEndpointHeader(XWPFDocument document, String method, String path, String access) {
        XWPFTable table = document.createTable(1, 4);
        table.setWidth("100%");
        XWPFTableRow row = table.getRow(0);
        
        // Method Badge
        XWPFTableCell methodCell = row.getCell(0);
        String[] colors = getMethodColors(method);
        fillCell(methodCell, method, true, colors[1], colors[0]);
        setCellWidth(methodCell, 1200);
        
        // Path
        XWPFTableCell pathCell = row.getCell(1);
        fillCell(pathCell, path, true, PRIMARY, null);
        setCellWidth(pathCell, 4000);
        
        // Access
        XWPFTableCell accessCell = row.getCell(3);
        fillCell(accessCell, "Access: " + access, false, "5D6D7E", null);
        setCellWidth(accessCell, 2000);
    }

    private void addSubSubTitle(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setFontSize(11);
        run.setColor(PRIMARY);
        run.setText(text);
    }

    // ─────────────────────────────────────────────────────────
    // JSON Syntax Highlighting 
    // ─────────────────────────────────────────────────────────

    private void createJsonBlock(XWPFDocument document, String json) {
        XWPFTable table = document.createTable(1, 1);
        table.setWidth("100%");
        
        // Border like JS code
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        CTTblBorders tblBorders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        setTableBorder(tblBorders, ACCENT, 6);

        XWPFTableCell cell = table.getRow(0).getCell(0);
        cell.setColor(JSON_BG);
        
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setSpacingBefore(50);
        para.setSpacingAfter(50);
        
        String[] lines = json.split("\n");
        for (int i = 0; i < lines.length; i++) {
            tokenizeJsonLine(para, lines[i]);
            if (i < lines.length - 1) {
                para.createRun().addBreak();
            }
        }
    }

    private void tokenizeJsonLine(XWPFParagraph para, String line) {
        // Simple regex-based tokenization for JSON
        String regex = "(\"(?:[^\"\\\\]|\\\\.)*\"\\s*:)|(\"(?:[^\"\\\\]|\\\\.)*\")|(true|false|null)|(-?\\d+(?:\\.\\d+)?)|([\\{\\}\\[\\]:,])|(\\s+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(line);
        
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                addJsonRun(para, line.substring(lastEnd, matcher.start()), JSON_PUNCT);
            }
            
            if (matcher.group(1) != null) { // Key
                String key = matcher.group(1);
                int colonIdx = key.lastIndexOf(':');
                addJsonRun(para, key.substring(0, colonIdx), JSON_KEY);
                addJsonRun(para, key.substring(colonIdx), JSON_PUNCT);
            } else if (matcher.group(2) != null) { // String value
                addJsonRun(para, matcher.group(2), JSON_STR);
            } else if (matcher.group(3) != null || matcher.group(4) != null) { // Literal/Num
                addJsonRun(para, matcher.group(), JSON_NUM);
            } else { // Punctuation / WS
                addJsonRun(para, matcher.group(), JSON_PUNCT);
            }
            lastEnd = matcher.end();
        }
    }

    private void addJsonRun(XWPFParagraph para, String text, String color) {
        XWPFRun run = para.createRun();
        run.setFontFamily("Courier New");
        run.setFontSize(9);
        run.setColor(color);
        run.setText(text);
    }

    private String generateExampleJson(ModuleConfig config, boolean isRequest) {
        StringBuilder sb = new StringBuilder("{\n");
        if (!isRequest) {
            sb.append("  \"").append(NamingUtil.decapitalize(NamingUtil.toEntityName(config))).append("Uuid\": \"01942e5a-1234-7abc-8def-abcdef123456\",\n");
        }
        
        for (FieldConfig f : config.getFields()) {
            if (isRequest && f.isExcludeFromRequest()) continue;
            if (!isRequest && f.isExcludeFromResponse()) continue;
            
            sb.append("  \"").append(f.getName()).append("\": ");
            String type = f.getType().toLowerCase();
            if (type.equals("string")) sb.append("\"example_value\"");
            else if (type.equals("integer") || type.equals("long")) sb.append("123");
            else if (type.equals("boolean")) sb.append("true");
            else if (type.equals("uuid")) sb.append("\"00000000-0000-0000-0000-000000000000\"");
            else sb.append("null");
            sb.append(",\n");
        }
        
        if (!isRequest) {
            sb.append("  \"status\": \"active\"\n");
        } else {
            // Remove last comma
            if (sb.lastIndexOf(",") > 0) {
                sb.setLength(sb.length() - 2);
                sb.append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────
    // Low-level UI Helpers
    // ─────────────────────────────────────────────────────────

    private void fillHeaderRow(XWPFTableRow row, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            fillCell(row.getCell(i), headers[i], true, "FFFFFF", PRIMARY);
        }
    }

    private void fillCell(XWPFTableCell cell, String text, boolean bold, String textColor, String fill) {
        if (fill != null) cell.setColor(fill);
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = para.createRun();
        run.setText(text != null ? text : "");
        run.setBold(bold);
        if (textColor != null) run.setColor(textColor);
        run.setFontSize(10);
    }

    private void fillInfoRow(XWPFTableRow row, String label, String value, boolean isAlt) {
        String fill = isAlt ? ROW_ALT : "FFFFFF";
        fillCell(row.getCell(0), label, true, PRIMARY, fill);
        fillCell(row.getCell(1), value, false, null, fill);
    }

    private void fillStatusRow(XWPFTableRow row, String code, String meaning, String usage, boolean isAlt) {
        String fill = isAlt ? ROW_ALT : "FFFFFF";
        fillCell(row.getCell(0), code, true, SUCCESS_TEXT, fill);
        fillCell(row.getCell(1), meaning, false, null, fill);
        fillCell(row.getCell(2), usage, false, "5D6D7E", fill);
    }

    private void createBadgeTable(XWPFDocument document, String label, String value, String color) {
        XWPFTable table = document.createTable(1, 2);
        table.setWidth("100%");
        XWPFTableRow row = table.getRow(0);
        
        fillCell(row.getCell(0), label, true, color, ROW_ALT);
        setCellWidth(row.getCell(0), 1500);
        fillCell(row.getCell(1), value, true, PRIMARY, ROW_ALT);
    }

    private String[] getMethodColors(String method) {
        return switch (method.toUpperCase()) {
            case "GET"    -> new String[]{SUCCESS_BG, SUCCESS_TEXT};
            case "POST"   -> new String[]{PURPLE_BG, PURPLE_TEXT};
            case "PUT"    -> new String[]{WARNING_BG, WARNING_TEXT};
            case "DELETE" -> new String[]{DANGER_BG, DANGER_TEXT};
            default       -> new String[]{ACCENT_LIGHT, ACCENT};
        };
    }

    private void setTableAlignment(XWPFTable table, STJcTable.Enum alignment) {
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        CTJcTable jc = tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc();
        jc.setVal(alignment);
    }

    private void setCellWidth(XWPFTableCell cell, int width) {
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth tblWidth = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        tblWidth.setW(BigInteger.valueOf(width));
        tblWidth.setType(STTblWidth.DXA);
    }

    private void setTableBorder(CTTblBorders borders, String color, int size) {
        CTBorder top = borders.isSetTop() ? borders.getTop() : borders.addNewTop();
        top.setVal(STBorder.SINGLE); top.setSz(BigInteger.valueOf(size)); top.setColor(color);
        
        CTBorder bottom = borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
        bottom.setVal(STBorder.SINGLE); bottom.setSz(BigInteger.valueOf(size)); bottom.setColor(color);
        
        CTBorder left = borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft();
        left.setVal(STBorder.SINGLE); left.setSz(BigInteger.valueOf(size)); left.setColor(color);
        
        CTBorder right = borders.isSetRight() ? borders.getRight() : borders.addNewRight();
        right.setVal(STBorder.SINGLE); right.setSz(BigInteger.valueOf(size)); right.setColor(color);
    }

    private void createRelationshipsTable(XWPFDocument document, ModuleConfig config) {
        List<RelationshipConfig> rels = config.getRelationships();
        XWPFTable table = document.createTable(rels.size() + 1, 4);
        table.setWidth("100%");
        fillHeaderRow(table.getRow(0), new String[]{"Name", "Type", "Target", "Mapping"});
        for (int i = 0; i < rels.size(); i++) {
            RelationshipConfig r = rels.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            boolean isAlt = i % 2 != 0;
            String fill = isAlt ? ROW_ALT : "FFFFFF";
            fillCell(row.getCell(0), r.getName(), true, PRIMARY, fill);
            fillCell(row.getCell(1), r.getType(), false, null, fill);
            fillCell(row.getCell(2), r.getTargetEntity(), false, null, fill);
            fillCell(row.getCell(3), r.getMappedBy() != null ? "mappedBy=" + r.getMappedBy() : "JoinColumn", false, null, fill);
        }
    }
}
