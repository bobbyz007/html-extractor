package org.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.Member;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        Util.write(workbook, args[0]);
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

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Type", 4000));
        CELL_HEADERS.add(new Util.Header("Title", 4000));
        CELL_HEADERS.add(new Util.Header("Address", 4000));
        CELL_HEADERS.add(new Util.Header("Telephone", 4000));
        CELL_HEADERS.add(new Util.Header("Email", 4000));
        CELL_HEADERS.add(new Util.Header("Web", 4000));
        CELL_HEADERS.add(new Util.Header("Twitter", 4000));
        CELL_HEADERS.add(new Util.Header("Contacts", 4000));
        CELL_HEADERS.add(new Util.Header("Description", 9000));
    }

    static Workbook export(List<Member> members) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

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

    static void convertDataToRow(Member member, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, member.getType());
        Util.createCell(cellNumber++, row, member.getTitle());
        Util.createCell(cellNumber++, row, member.getAddress());
        Util.createCell(cellNumber++, row, member.getTelephone());

        String email = member.getEmail();
        if (StringUtils.startsWithIgnoreCase(email, "mailto:")) {
            email = email.substring(7);
        }
        Util.createCell(cellNumber++, row, StringUtils.trim(email));
        // comment as email may not be valid. TODO: Aad validation
        /*Cell cell = createCell(cellNumber++, row, member.getEmail());
        Hyperlink link = row.getSheet().getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.EMAIL);
        link.setAddress(member.getEmail());
        cell.setHyperlink(link);*/

        Util.createCell(cellNumber++, row, member.getWebUrl());
        Util.createCell(cellNumber++, row, member.getTwitterAccount());
        Util.createCell(cellNumber++, row, member.getContacts());
        Util.createCell(cellNumber++, row, member.getDesc());
    }




}

