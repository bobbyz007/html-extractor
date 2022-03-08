package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.GreenValleyReseller;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GreenValleyResellerExtractor {
    static final String SCODE_CONTINENT = "68";
    static final String URL_TPL_NAV = "https://www.greenvalleyintl.com/api.php/cms/nav/scode/%s";
    static final String URL_TPL_RESELLER = "https://www.greenvalleyintl.com/api.php/list/%s";

    static final CloseableHttpClient httpClient = SeeSSLCloseableHttpClient.getCloseableHttpClient();

    public static void main(String[] args) throws IOException, InterruptedException {
        List<GreenValleyReseller> resellers = new ArrayList<>();
        String continentJsonStr = get(String.format(URL_TPL_NAV, SCODE_CONTINENT));
        JSONObject continentJson = JSON.parseObject(continentJsonStr);
        JSONArray continentArr = continentJson.getJSONArray("data");
        for (Object obj : continentArr) {
            JSONObject continentObj = (JSONObject) obj;
            String continentCode = continentObj.getString("scode");
            String continentName = continentObj.getString("name");

            String countryJsonStr = get(String.format(URL_TPL_NAV, continentCode));
            JSONObject countryJson = JSON.parseObject(countryJsonStr);
            JSONArray countryArr = countryJson.getJSONArray("data");

            for (Object obj2 : countryArr) {
                JSONObject countryObj = (JSONObject) obj2;
                String countryCode = countryObj.getString("scode");
                String countryName = countryObj.getString("name");

                String resellerJsonStr = get(String.format(URL_TPL_RESELLER, countryCode));
                JSONObject resellerJson = JSON.parseObject(resellerJsonStr);
                JSONArray resellerArr = resellerJson.getJSONArray("data");

                for (Object obj3 : resellerArr) {
                    JSONObject resellerObj = (JSONObject) obj3;
                    String title = resellerObj.getString("title");
                    String data = resellerObj.getString("content");

                    Document doc = Jsoup.parse(data);
                    Elements pEles = doc.select("p");
                    StringBuilder sb = new StringBuilder();
                    for (Element pEle : pEles) {
                        sb.append(pEle.text() + System.lineSeparator());
                    }

                    GreenValleyReseller reseller = new GreenValleyReseller(continentName, countryName, title, sb.toString());
                    resellers.add(reseller);
                }
            }
        }

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(resellers);
        Util.write(workbook, "result/greenvalley-reseller-result.xlsx");
    }

    static String get(String url) throws IOException {
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

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Continent", 4000));
        CELL_HEADERS.add(new Util.Header("Country", 4000));
        CELL_HEADERS.add(new Util.Header("Title", 4000));
        CELL_HEADERS.add(new Util.Header("Reseller", 20000));
    }

    static Workbook export(List<GreenValleyReseller> resellers) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (GreenValleyReseller reseller : resellers) {
            if (reseller == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(reseller, row);
        }
        return workbook;
    }

    static void convertDataToRow(GreenValleyReseller reseller, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, reseller.getContinent());
        Util.createCell(cellNumber++, row, reseller.getCountry());
        Util.createCell(cellNumber++, row, reseller.getTitle());
        Util.createCell(cellNumber++, row, reseller.getReseller());
    }
}
