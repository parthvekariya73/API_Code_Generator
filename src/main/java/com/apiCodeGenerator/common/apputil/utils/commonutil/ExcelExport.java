package com.apiCodeGenerator.common.apputil.utils.commonutil;

import com.apiCodeGenerator.common.apputil.utils.db.DBUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.List;
import java.util.Map;

@Component
public class ExcelExport {

    private static final Logger log = LoggerFactory.getLogger(ExcelExport.class);

    private final DateAndTimeUtils dATUtils;
    private final DBUtils dbUtils;

    public ExcelExport(DateAndTimeUtils dATUtils, DBUtils dbUtils) {
        this.dATUtils = dATUtils;
        this.dbUtils = dbUtils;
    }

    public void usingQuery(String[] columns, String basicDetailsQry, Map<String, Object> filterMap, String sheetName, Map<String, Object> responseMap) throws Exception {
        List<Object[]> basicDetailList = dbUtils.getResults(basicDetailsQry, filterMap).stream().map(m -> m).toList();
        if(basicDetailList != null && !basicDetailList.isEmpty()) {
            filterMap.clear();
            responseMap.put("excelData", generateExcel(columns, basicDetailList, sheetName)) ;
        }
    }


    public ExcelExportResult generateExcel(String[] columns, List<Object[]> dataList, String sheetName) {
        ExcelExportResult result = new ExcelExportResult();

        if (columns == null || columns.length == 0 || dataList == null || dataList.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("Invalid input data");
            return result;
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            workbook.setCompressTempFiles(true);
            CreationHelper createHelper = workbook.getCreationHelper();

            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));

            CellStyle timeCellStyle = workbook.createCellStyle();
            timeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("h:mm"));

            CellStyle dateTimeCellStyle = workbook.createCellStyle();
            dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));

            CellStyle headerStyle = createHeaderStyle(workbook);

            Sheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            int rowNum = 1;
            for (Object[] dataRow : dataList) {
                Row row = sheet.createRow(rowNum++);
                for (int col = 0; col < dataRow.length; col++) {
                    Object value = dataRow[col];
                    Cell cell = row.createCell(col);
                    setCellValue(cell, value, dateCellStyle, timeCellStyle, dateTimeCellStyle);
                }
            }

            workbook.write(baos);
            baos.flush();

            result.setSuccess(true);
            result.setFileData(baos.toByteArray());

        } catch (Exception e) {
            log.error("Excel generation failed", e);
            result.setSuccess(false);
            result.setErrorMessage("Failed to generate Excel file");
        }
        return result;
    }


    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        style.setRightBorderColor(IndexedColors.WHITE.getIndex());
        style.setTopBorderColor(IndexedColors.WHITE.getIndex());

        Font font = wb.createFont();
        font.setFontName("Calibri");
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private void setCellValue(Cell cell, Object value, CellStyle dateCellStyle, CellStyle timeCellStyle, CellStyle dateTimeCellStyle) throws SQLException {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof String || value instanceof Character) {
            cell.setCellValue(value.toString());
        } else if (value instanceof Timestamp) {
            cell.setCellValue(dATUtils.convertToJavaDate((Timestamp) value));
            cell.setCellStyle(dateTimeCellStyle);
        } else if (value instanceof Time) {
            cell.setCellValue(dATUtils.convertToJavaDate((Time) value));
            cell.setCellStyle(timeCellStyle);
        } else if (value instanceof Date) {
            cell.setCellValue(dATUtils.convertToJavaDate((Date) value));
            cell.setCellStyle(dateCellStyle);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Clob) {
            Clob clob = (Clob) value;
            long length = Math.min(clob.length(), 32767);
            cell.setCellValue(clob.getSubString(1, (int) length));
        } else {
            // Fallback for other types
            cell.setCellValue(value.toString());
        }
    }


}
