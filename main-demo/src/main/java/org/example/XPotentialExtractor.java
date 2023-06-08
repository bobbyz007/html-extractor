package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.PotentialExhibitor;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XPotentialExtractor {
    static final String URL_PREFIX = "https://www.xponential.org/xponential2023/public/";
    static final String URL_INDEX_EXHIBITOR = "https://www.xponential.org/xponential2023/public/Exhibitors.aspx?CatID=17";

    public static void main(String[] args) throws InterruptedException, IOException {
        List<PotentialExhibitor> exhibitors = extractExihibitorList();

        // 获取exhibitor的detail信息，填充products和contacts字段
        for (int i = 0; i < exhibitors.size(); i++) {
            System.out.println("Extract details for item: " + (i+1));
            fillDetails(exhibitors.get(i));
        }
        Workbook workbook = export(exhibitors);
        Util.write(workbook, "result/xpotential-exhibitor.xlsx");
    }

    static List<PotentialExhibitor> extractExihibitorList() throws IOException {
        List<PotentialExhibitor> exhibitors = new ArrayList<>();

        String htmlStr = Jsoup.connect(URL_INDEX_EXHIBITOR)
                .timeout(100000)
                .get().outerHtml();
        Document html = Jsoup.parse(htmlStr);

        Element tableEles = html.selectFirst("div.listTableBody > table > tbody");
        for (Element trEle : tableEles.children()) {
            Element nameEle = trEle.selectFirst("td.companyName > a.exhibitorName");
            Element boothEle = trEle.selectFirst("td.boothLabel > a");
            PotentialExhibitor exhibitor = new PotentialExhibitor(
                    nameEle.text(), boothEle.text(), null, null, URL_PREFIX + nameEle.attr("href"));
            exhibitors.add(exhibitor);
        }

        return exhibitors;
    }

    static void fillDetails(PotentialExhibitor exhibitor) throws IOException {
        String htmlStr = Jsoup.connect(exhibitor.getUrl())
                .timeout(100000)
                .get().outerHtml();
        Document html = Jsoup.parse(htmlStr);
        Element contactEle = html.selectFirst("div.BoothContactInfo");
        if (contactEle != null) {
            exhibitor.setContact(contactEle.text());
        }

        Element profileEle = html.selectFirst("p.BoothPrintProfile");
        if (profileEle != null) {
            exhibitor.setProfile(profileEle.text());
        }
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Name", 8000));
        CELL_HEADERS.add(new Util.Header("Booth", 3000));
        CELL_HEADERS.add(new Util.Header("Contact", 12000));
        CELL_HEADERS.add(new Util.Header("Profile", 12000));
    }

    static Workbook export(List<PotentialExhibitor> exhibitors) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (PotentialExhibitor exhibitor : exhibitors) {
            if (exhibitor == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(exhibitor, row);
        }
        return workbook;
    }

    static void convertDataToRow(PotentialExhibitor store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getBooth());
        Util.createCell(cellNumber++, row, store.getContact());
        Util.createCell(cellNumber++, row, store.getProfile());
    }
}



