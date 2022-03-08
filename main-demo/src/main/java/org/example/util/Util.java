package org.example.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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

    public static void write(Workbook workbook, String outputFilePath) {
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

    public static Sheet buildSheet(Workbook workbook, List<Header> headers) {
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

    public static Cell createCell(int cellNumber, Row row, String value) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(StringUtils.isNotBlank(value) ? value : "");
        return cell;
    }

    static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    public static String post(String url, String jsonParams) throws IOException {
        HttpPost post = new HttpPost(url);
        // 建立一个NameValuePair数组，用于存储欲传送的参数
        post.addHeader("Content-type","application/json; charset=utf-8");
        post.setHeader("Accept", "application/json");
        post.setEntity(new StringEntity(jsonParams, Charset.forName("UTF-8")));

        HttpResponse response = httpClient.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Err occurred.");
            return null;
        }

        String body = EntityUtils.toString(response.getEntity());
        return body;
    }

    public static String get(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        // 建立一个NameValuePair数组，用于存储欲传送的参数
        get.addHeader("Content-type","application/json; charset=utf-8");
        get.setHeader("Accept", "application/json");

        HttpResponse response = httpClient.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Err occurred.");
            return null;
        }

        String body = EntityUtils.toString(response.getEntity());
        return body;
    }
}
