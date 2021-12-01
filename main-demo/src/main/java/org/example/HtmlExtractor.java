package org.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.Member;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HtmlExtractor {
    // to avoid treated as hackers
    static final int SLEEP_MILLIS = 1500;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args == null || args.length != 1) {
            System.err.println("usage: -jar xxx.jar [output file path]");
            return;
        }
        List<Member> resultMembers = fetchHtmlContent("https://www.tsa-uk.org.uk/tsa_members/");
        // List<Member> resultMembers = Arrays.asList(Member.mock(3));

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(resultMembers);
        write(workbook, args[0]);
    }

    static List<Member> fetchHtmlContent(String url) throws InterruptedException, IOException {
        List<Member> resultMembers = new ArrayList<>();

        Document document = Jsoup.connect(url).get();
        Element pageEle = document.selectFirst("div#page");
        Element sectionEle = pageEle.selectFirst("section.main");

        Element contentEle = sectionEle.selectFirst("section > div > div > div.twelve");

        for (Element h2Ele : contentEle.select("h2")) {
            String memberType = h2Ele.text();
            Element memberListDiv = h2Ele.nextElementSibling();

            System.out.println("Start extracting: " + memberType);
            // 遍历每一个member list
            for (Element memberHref : memberListDiv.select("div.member > a")) {
                String memberUrl = memberHref.attr("href");
                Member member = parseMember(memberType, memberUrl);
                resultMembers.add(member);
            }

            System.out.println("End extracting successfully: " + memberType);
            Thread.sleep(SLEEP_MILLIS);
        }

        return resultMembers;
    }

    static Member parseMember(String memberType, String url) throws IOException {
        Document memberDoc = Jsoup.connect(url).get();
        Element pageEle = memberDoc.selectFirst("div#page");
        Element sectionEle = pageEle.selectFirst("section.main");

        Element titleDiv = sectionEle.selectFirst("section > div > div > div.twelve");
        Element contentDiv = null;
        if (titleDiv != null) {
            contentDiv = titleDiv.parent().nextElementSibling();
        } else {
            contentDiv = sectionEle.selectFirst("section > div > div");
        }

        String title = titleDiv.selectFirst("h1").text();

        String desc = contentDiv.selectFirst("div.six").wholeText();

        Element detailsDiv = contentDiv.selectFirst("div > div.job-details");

        Member member = new Member(memberType, title, desc.trim());
        for (Element h6Ele : detailsDiv.select("h6")) {
            String fieldName = h6Ele.text();
            String fieldValue = h6Ele.nextElementSibling().text();
            // extract mailto
            if (fieldName.equals(Member.FIELD_EMAIL)) {
                fieldValue = h6Ele.nextElementSibling().selectFirst("a").attr("href");
            }
            member.setValue(fieldName, fieldValue);
        }

        return member;
    }

    static List<String> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add("Type");
        CELL_HEADERS.add("Title");
        CELL_HEADERS.add("Address");
        CELL_HEADERS.add("Telephone");
        CELL_HEADERS.add("Email");
        CELL_HEADERS.add("Web");
        CELL_HEADERS.add("Twitter");
        CELL_HEADERS.add("Contacts");
        CELL_HEADERS.add("Description");
    }

    static Workbook export(List<Member> members) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = buildSheet(workbook);

        //构建每行的数据内容
        int rowNum = 1;
        for (Member member : members) {
            if (member == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(member, row);
        }
        return workbook;
    }

    static Sheet buildSheet(Workbook workbook) {
        // build data sheet
        Sheet sheet = workbook.createSheet();
        CellStyle textCellStyle= workbook.createCellStyle();
        textCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("@"));

        for (int i = 0; i < CELL_HEADERS.size(); i++) {
            if (i == CELL_HEADERS.size() - 1) {
                sheet.setColumnWidth(i, 9000);
            } else {
                sheet.setColumnWidth(i, 4000);
            }

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
        for (int i = 0; i < CELL_HEADERS.size(); i++) {
            Cell cell = head.createCell(i);
            cell.setCellValue(CELL_HEADERS.get(i));
            cell.setCellStyle(cellStyle);
        }
        return sheet;
    }

    static void convertDataToRow(Member member, Row row) {
        int cellNumber = 0;
        createCell(cellNumber++, row, member.getType());
        createCell(cellNumber++, row, member.getTitle());
        createCell(cellNumber++, row, member.getAddress());
        createCell(cellNumber++, row, member.getTelephone());

        String email = member.getEmail();
        if (StringUtils.startsWithIgnoreCase(email, "mailto:")) {
            email = email.substring(7);
        }
        createCell(cellNumber++, row, StringUtils.trim(email));
        // comment as email may not be valid. TODO: Aad validation
        /*Cell cell = createCell(cellNumber++, row, member.getEmail());
        Hyperlink link = row.getSheet().getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.EMAIL);
        link.setAddress(member.getEmail());
        cell.setHyperlink(link);*/

        createCell(cellNumber++, row, member.getWebUrl());
        createCell(cellNumber++, row, member.getTwitterAccount());
        createCell(cellNumber++, row, member.getContacts());
        createCell(cellNumber++, row, member.getDesc());
    }

    static Cell createCell(int cellNumber, Row row, String value) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(StringUtils.isNotBlank(value) ? value : "");
        return cell;
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
}

