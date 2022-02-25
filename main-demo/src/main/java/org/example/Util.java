package org.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Util {
    public static class Header {
        String title;
        int width;

        public Header(String title, int width) {
            this.title = title;
            this.width = width;
        }
    }

    static void write(Workbook workbook, String outputFilePath) {
        // 以文件的形式输出工作簿对象
        FileOutputStream fileOut = null;
        try {
            File exportFile = new File(outputFilePath);
            if (!exportFile.exists()) {
                exportFile.createNewFile();
            }

            fileOut = new FileOutputStream(exportFile);
            workbook.write(fileOut);
            fileOut.flush();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (null != fileOut) {
                    fileOut.close();
                }
                if (null != workbook) {
                    workbook.close();
                }
            } catch (IOException e) {
                System.err.println("关闭输出流时发生错误，错误原因：" + e.getMessage());
            }
        }
    }

    static Sheet buildSheet(Workbook workbook, List<Header> headers) {
        // build data sheet
        Sheet sheet = workbook.createSheet();
        CellStyle textCellStyle= workbook.createCellStyle();
        textCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));

        for (int i = 0; i < headers.size(); i++) {
            sheet.setColumnWidth(i, headers.get(i).width);
            sheet.setDefaultColumnStyle(i, textCellStyle);
        }
        sheet.setDefaultRowHeight((short) 400);

        CellStyle cellStyle = workbook.createCellStyle();
        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 边框颜色和宽度设置
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex()); // 下边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex()); // 左边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex()); // 右边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex()); // 上边框
        // 设置背景颜色
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 字体设置
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 16);
        cellStyle.setFont(font);

        Row head = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = head.createCell(i);
            cell.setCellValue(headers.get(i).title);
            cell.setCellStyle(cellStyle);
        }
        return sheet;
    }

    static Cell createCell(int cellNumber, Row row, String value) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(StringUtils.isNotBlank(value) ? value : "");
        return cell;
    }

    public static void main(String[] args) {
        String name = "Mapping/GISout/sdf".replaceAll("[\\\\/]", " ");
        System.out.println(name);
    }
}
