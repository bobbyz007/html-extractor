package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.AutelStore;
import org.example.entity.Member;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AutelStoreExtractor {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<AutelStore> resultStores = fetchHtmlContent("D:\\workspace\\opensource\\html-extractor\\main-demo\\src\\main\\resources\\data\\autel_robotics.html");
        Workbook workbook = export(resultStores);
        Util.write(workbook, "result/autel-store-result.xlsx");
    }

    static List<AutelStore> fetchHtmlContent(String path) throws InterruptedException, IOException {
        List<AutelStore> resultStores = new ArrayList<>();

        Document document = Jsoup.parse(new File(path), StandardCharsets.UTF_8.name());
        Element ulEle = document.selectFirst("ul.agent-list-mapUl");

        for (Element liEle : ulEle.select("li.agent-list-mapLi")) {
            Element containerDiv = liEle.selectFirst("div");
            AutelStore store = new AutelStore();
            resultStores.add(store);
            store.setName(containerDiv.selectFirst("h3").text());
            for (Element pEle : containerDiv.select("p")) {
                String pContent = pEle.text();
                int colonIndex = pContent.indexOf(":");
                if (colonIndex <= 0) {
                    continue;
                }
                String title = pContent.substring(0, colonIndex);
                String content = pContent.substring(colonIndex + 1);
                switch (title) {
                    case "Address":
                        store.setAddress(content);
                        break;
                    case "Phone":
                        store.setPhone(content);
                        break;
                    case "Email":
                        store.setEmail(content);
                        break;
                    case "Website":
                        store.setWebsite(content);
                }
            }

        }

        return resultStores;
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
        CELL_HEADERS.add(new Util.Header("Name", 5000));
        CELL_HEADERS.add(new Util.Header("Address", 14000));
        CELL_HEADERS.add(new Util.Header("Phone", 5000));
        CELL_HEADERS.add(new Util.Header("Email", 6000));
        CELL_HEADERS.add(new Util.Header("Website", 10000));
    }

    static void convertDataToRow(AutelStore store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getAddress());
        Util.createCell(cellNumber++, row, store.getPhone());
        Util.createCell(cellNumber++, row, store.getEmail());
        Util.createCell(cellNumber++, row, store.getWebsite());
    }

    static Workbook export(List<AutelStore> stores) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (AutelStore store : stores) {
            if (store == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(store, row);
        }
        return workbook;
    }
}

