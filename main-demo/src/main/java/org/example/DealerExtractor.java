package org.example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.Dealer;
import org.example.util.Util;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DealerExtractor {
    static final int PAGE_START = 31;
    static final int PAGE_END = 32;
    static final String URL_TPL = "https://www.controller.com/dealer/directory?Page=%s";

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Dealer> dealers = new ArrayList<>();
        for (int i = PAGE_START; i <= PAGE_END; ++i) {
            String url = String.format(URL_TPL, i);
            dealers.addAll(fetchHtmlContent(url));
        }

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(dealers);
        Util.write(workbook, "result/dealer-result-4.xlsx");
    }

    static List<Dealer> fetchHtmlContent(String url) throws InterruptedException, IOException {
        List<Dealer> resultDealers = new ArrayList<>();

        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")
                .referrer("https://www.controller.com/")
                .header("cookie", "geoipcountryid=id=36; MapHistory=set=True; __RequestVerificationToken=pxmNqVM5MnCJdBfxqQB7TVlFdRoZqtO8Jia1G-j43RXe6hm7Z4EMMkFue6vidfK2mdHaCw2; BIGipServerwww.controller.tradesites_http_pool=3347097792.20480.0000; ASP.NET_SessionId=ahuu4tpnja5cbbgitarfinom; _hjSessionUser_1143839=eyJpZCI6IjY2NTNiYTQwLThjOTktNWJiNC1hMmE3LTZlZmNkZTUxNDNmNSIsImNyZWF0ZWQiOjE2NDUxNjA0Mjg2NjgsImV4aXN0aW5nIjp0cnVlfQ==; __gads=ID=168a0722a565ba84:T=1645160757:S=ALNI_MYVKWr3hYXAjjacRu9nLOm0wdcU9Q; _gid=GA1.2.1966155982.1645599083; UserID=ID=BehMY9fXYgkV%2bnzrSHNjsk0cSqNH%2fqWZ0IAmlbov2bysg%2bXOYhLKcMsnLCt3DES%2f6CUNrVS56samNPPEAilCmw%3d%3d&LV=dUl81IalZwlZi2hthSFML5XF%2fj49gtwB%2bSAd9X3Nhs8lVc5wWNSyAEzMPJAwm80WPvovfcdr7e5FGEtBAKEr8Isii%2fK9D67Z; Tracking=SessionStarted=1&UserReferrer=https%3a%2f%2fwww.controller.com%2fdealer%2fdirectory%3fPage%3d7&GUID=8086012864294558185; _hjIncludedInSessionSample=1; _hjSession_1143839=eyJpZCI6Ijg3NzJmZTgyLTZhOGEtNDI2NC1iNTdmLTUxZTE4M2NjZjJiMyIsImNyZWF0ZWQiOjE2NDU2MTA1NDI4NjQsImluU2FtcGxlIjp0cnVlfQ==; _hjAbsoluteSessionInProgress=0; _ga=GA1.1.1163264693.1645160428; _uetsid=4addae90947511ecb5aee7297cb4e1d2; _uetvid=b1793eb0907711ec990535c819cf047b; UserSettingsCookie=screenSize=1671|274; _ga_8LG8ZS4P9J=GS1.1.1645610536.4.1.1645610631.60; reese84=3:gq/IrGXX/4E41LRq17KoOg==:lVBgq4/snptJCJzkAIyC3sC85ZhoVMuxRWmH87vV0hqLbCA/BvqYoDwmX1NU9pqrKtyMQBniOrywh4jEibzmBtEeOfEPgKYhTFNTTr134AZ5cHxtghVieZI9VmFqY71sbQkjDFcL2z6XIUa9W7yGjvo+2DlrfjvASbQjEpiCho2gGJzNpHJERaFdpEZYlXy1ZXpUdcu3SBdtr8GexiNwIzKWzzcafWm5lR8H1sGBCoVV5EnxgBFrFGKFZWLx8R1vDXoqe4Z8icUUQTexFL/NVajLTPEbTijLWfC1YzJjHLOXpNUVzs2gsdz/xXfQ9e8CuT9dSCSQWtF4u0E+5pAWPzBGhYlzoj606HCAhF74dfteSNRIWF0IXQclA9Berno7BtjmMMZYkZqUk1zBkdGRsUoxTiJ0b10MWh+h87MpAts=:Dkac+eosV0H0To3kPd4U0V905NsfG2gHjjdPd6pCQVk=")
                .header("upgrade-insecure-requests", "1");

        Document document = connection.get();

        Element dealerDirContent = document.selectFirst("div#dealer-directory-content");

        Element dealerWrapper = dealerDirContent.selectFirst("div.dealer-listings-wrapper");

        Elements dealerEles = dealerWrapper.select("div.dealer-directory-listing");
        for (Element dealerEle : dealerEles) {
            Element contactEle = dealerEle.selectFirst("div.dealer-contact-info");
            Element aEle = contactEle.selectFirst("div > a");
            String title = null;
            if (aEle == null) {
                Element spanEle = contactEle.selectFirst("div > span");
                if (spanEle != null) {
                    title = spanEle.text();
                }
            } else {
                title = aEle.text();
            }
            String data = contactEle.select("div.dealer-data-text").text();

            Element phoneEle = dealerEle.selectFirst("div.button-container > a");
            String phone = null;
            if (phoneEle != null) {
                phone = phoneEle.text();
            }
            Dealer dealer = new Dealer(title, data, phone);
            resultDealers.add(dealer);
        }

        return resultDealers;
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Title", 4000));
        CELL_HEADERS.add(new Util.Header("Data", 4000));
        CELL_HEADERS.add(new Util.Header("Phone", 4000));
    }

    static Workbook export(List<Dealer> dealers) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (Dealer dealer : dealers) {
            if (dealer == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(dealer, row);
        }
        return workbook;
    }

    static void convertDataToRow(Dealer dealer, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, dealer.getTitle());
        Util.createCell(cellNumber++, row, dealer.getData());
        Util.createCell(cellNumber++, row, dealer.getPhone());
    }
}

