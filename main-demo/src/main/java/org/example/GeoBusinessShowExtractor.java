package org.example;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.GeoExhibitor;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeoBusinessShowExtractor {
    static final String[] FILES_HTML = new String[]{"geobusinessshow-1.html", "geobusinessshow-2.html", "geobusinessshow-3.html"};

    public static void main(String[] args) throws InterruptedException, IOException {
        List<GeoExhibitor> exhibitors = new ArrayList<>();

        for (String file : FILES_HTML) {
            Document html = Jsoup.parse(FileUtils.readFileToString(
                    new File("/home/justin/IdeaProjects/html-extractor/main-demo/src/main/resources/data", file),
                    StandardCharsets.UTF_8));

            Element parentDiv = html.selectFirst("div.drts-gutter-sm");
            for (Element div : parentDiv.children()) {
                GeoExhibitor exhibitor = new GeoExhibitor();
                Elements elements = div.select("div > div > div > div.drts-row > div[data-name=column]");
                Element columnDiv = elements.get(1);
                Element aElement = columnDiv.selectFirst("div[data-name=entity_field_post_title] > a");
                exhibitor.setName(aElement.text());

                Element standElement = columnDiv.selectFirst("div[data-name=entity_field_field_stand_number]");
                exhibitor.setStandNumber(standElement != null ? standElement.text() : "");

                Element descElement = columnDiv.selectFirst("div[data-name=group] > div[data-name=entity_field_field_company_profile]");
                exhibitor.setDesc(descElement != null ? descElement.text() : "");

                exhibitors.add(exhibitor);
            }
        }

        // output result members
        Workbook workbook = export(exhibitors);
        Util.write(workbook, "result/geo-business-exhibitor.xlsx");
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Name", 8000));
        CELL_HEADERS.add(new Util.Header("Stand Number", 5000));
        CELL_HEADERS.add(new Util.Header("Company Profile", 25000));
    }

    static Workbook export(List<GeoExhibitor> exhibitors) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (GeoExhibitor exhibitor : exhibitors) {
            if (exhibitor == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(exhibitor, row);
        }
        return workbook;
    }

    static void convertDataToRow(GeoExhibitor store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getStandNumber());
        Util.createCell(cellNumber++, row, store.getDesc());
    }
}



