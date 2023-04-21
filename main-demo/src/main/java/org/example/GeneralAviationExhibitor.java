package org.example;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.AviationExhibitor;
import org.example.entity.GeoExhibitor;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeneralAviationExhibitor {
    static final String URL_HOST = "https://www.aero-expo.com";
    static final String URL_INDEX_EXHIBITOR = "https://www.aero-expo.com/list-of-exhibitors/index-of-exhibitors";
    static List<String> azFilterList;

    static void extractAZ() throws IOException {
        File azFilterFile = new File("/home/justin/IdeaProjects/html-extractor/main-demo/src/main/resources/data/azFilter.txt");
        if (azFilterFile.exists()) {
            azFilterList = FileUtils.readLines(azFilterFile, StandardCharsets.UTF_8);
            return;
        }

        azFilterList = new ArrayList<>();
        String htmlStr = Jsoup.connect(URL_INDEX_EXHIBITOR)
                .timeout(100000)
                .get().outerHtml();
        Document html = Jsoup.parse(htmlStr);
        Element azFilterDiv = html.selectFirst("div.azFilter");
        for (Element aEle : azFilterDiv.children()) {
            String href = aEle.attr("href");
            azFilterList.add(URL_HOST + href);
        }

        FileUtils.writeLines(azFilterFile, azFilterList);
    }

    static List<AviationExhibitor> extractExihibitorList(String url) throws IOException {
        List<AviationExhibitor> exhibitors = new ArrayList<>();

        String htmlStr = Jsoup.connect(url)
                .timeout(100000)
                .get().outerHtml();
        Document html = Jsoup.parse(htmlStr);

        Element eles = html.selectFirst("div.blockElementsAVZ");
        for (Element divEle : eles.children()) {
            Element contentEle = divEle.selectFirst("div > div.content");
            Element categoryEle = contentEle.selectFirst("div.category");
            Element headlineEle = contentEle.selectFirst("div.headline > a");
            Element detailsEle = contentEle.selectFirst("a.intern");
            AviationExhibitor exhibitor = new AviationExhibitor(
                    headlineEle.text(), categoryEle.text(), null, null, URL_HOST + detailsEle.attr("href"));
            exhibitors.add(exhibitor);
        }

        return exhibitors;
    }

    static void fillDetails(AviationExhibitor exhibitor) throws IOException {
        String htmlStr = Jsoup.connect(exhibitor.getDetailsUrl())
                .timeout(100000)
                .get().outerHtml();
        Document html = Jsoup.parse(htmlStr);
        Element productEle = html.selectFirst("div.elementDescription");
        exhibitor.setProducts(productEle.ownText());

        Element contactEle = html.selectFirst("div.elementKontakt");
        Element leftContantEle = contactEle.selectFirst("div>div.contactleft");
        exhibitor.setContact(leftContantEle.text());
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        extractAZ();

        File outputFile = new File("result/general-aviation-exhibitor(short).xlsx");
        List<AviationExhibitor> exhibitors = new ArrayList<>();
        if (!outputFile.exists()) {
            for (int i = 0; i < azFilterList.size(); i++) {
                exhibitors.addAll(extractExihibitorList(azFilterList.get(i)));
            }

            Workbook workbook = export(exhibitors);
            Util.write(workbook, outputFile.getPath());
        } else {
            EasyExcel.read(outputFile, AviationExhibitor.class, new AnalysisEventListener<AviationExhibitor>() {
                @Override
                public void invoke(AviationExhibitor data, AnalysisContext context) {
                    exhibitors.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).doReadAll();
        }

        // 获取exhibitor的detail信息，填充products和contacts字段
        for (int i = 0; i < exhibitors.size(); i++) {
            System.out.println("Extract details for item: " + (i+1));
            fillDetails(exhibitors.get(i));
        }
        Workbook workbook = export(exhibitors);
        Util.write(workbook, "result/general-aviation-exhibitor(full).xlsx");
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Name", 8000));
        CELL_HEADERS.add(new Util.Header("Stand Number", 5000));
        CELL_HEADERS.add(new Util.Header("Products", 10000));
        CELL_HEADERS.add(new Util.Header("Contact", 10000));
        CELL_HEADERS.add(new Util.Header("Details", 10000));

    }

    static Workbook export(List<AviationExhibitor> exhibitors) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (AviationExhibitor exhibitor : exhibitors) {
            if (exhibitor == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(exhibitor, row);
        }
        return workbook;
    }

    static void convertDataToRow(AviationExhibitor store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getStandNumber());
        Util.createCell(cellNumber++, row, store.getProducts());
        Util.createCell(cellNumber++, row, store.getContact());
        Util.createCell(cellNumber++, row, store.getDetailsUrl());
    }
}



