package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.DjiStore;
import org.example.entity.TrimbleDealer;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrimbleDealerExtractor {
    static final String URL_TPL_DEALERS = "https://code.metalocator.com/index.php?user_lat=0&user_lng=0" +
            "&postal_code=&radius=200&country=%s" +
            "&ml_location_override=&Itemid=15074&view=directory&layout=combined_bootstrap&tmpl=component" +
            "&framed=1&ml_skip_interstitial=0&preview=0&parent_table=&parent_id=0&task=search_zip" +
            "&search_type=point&_opt_out=0&option=com_locator&limitstart=%s&filter_order=id&filter_order_Dir=asc";

    static final String DATA_PLACEHOLDER = "var location_data =";

    static final List<String> COUNTRY_LIST = initCountryList();
    static final int PAGE_STEP = 10;

    static List<String> initCountryList() {
        List<String> resultList = new ArrayList<>();
        try {
            Document select = Jsoup.parse(FileUtils.readFileToString(
                    new File("D:\\workspace\\html-extractor\\main-demo\\src\\main\\resources\\data\\trimble-dealer-country-list.html"),
                    StandardCharsets.UTF_8));
            Elements options = select.select("option");
            for (Element option : options) {
                resultList.add(option.attr("value"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List<TrimbleDealer> dealers = new ArrayList<>();

        for (String country : COUNTRY_LIST) {
            System.out.println("fetch country: " + country);
            int pageStart = 0;
            while (true) {
                System.out.println("page start: " + pageStart);
                String html = Jsoup.connect(String.format(URL_TPL_DEALERS, country, pageStart))
                        .get().outerHtml();
                String locationDataJsonStr = ""; // extract from html
                for (String line : IOUtils.readLines(new StringReader(html))) {
                    line = line.trim();
                    if (line.startsWith(DATA_PLACEHOLDER)) {
                        // remove semicolon ;
                        locationDataJsonStr = line.substring(DATA_PLACEHOLDER.length(), line.length() - 1);
                    }
                }

                JSONArray dealerArr = JSON.parseArray(locationDataJsonStr);
                // reach page ends
                if (dealerArr.size() == 0) {
                    break;
                }

                // 从json 数组中解析出数据
                for (Object obj : dealerArr) {
                    JSONObject jo = (JSONObject) obj;
                    TrimbleDealer dealer = new TrimbleDealer();
                    dealer.setName(jo.getString("name"));
                    dealer.setAddress(String.format("%s %s %s",
                            jo.getString("address"),
                            jo.getString("address2"),
                            jo.getString("city")));
                    dealer.setCountry(jo.getString("country"));
                    dealer.setLink(jo.getString("link"));
                    dealer.setEmail(jo.getString("email"));
                    dealer.setPhone(jo.getString("phone"));
                    dealer.setPriorityName(jo.getString("priority_name"));

                    dealers.add(dealer);
                }

                pageStart += PAGE_STEP;
            }

            // sleep seconds between country
            Thread.sleep(2000);
        }

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(dealers);
        Util.write(workbook, "/home/justin/workspace/html-extractor/result/trimble-dealers.xlsx");
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Name", 3000));
        CELL_HEADERS.add(new Util.Header("Address", 15000));
        CELL_HEADERS.add(new Util.Header("country", 1000));
        CELL_HEADERS.add(new Util.Header("Email", 2000));
        CELL_HEADERS.add(new Util.Header("Link", 3000));
        CELL_HEADERS.add(new Util.Header("Phone", 3000));
        CELL_HEADERS.add(new Util.Header("Specialty", 3000));
    }

    static Workbook export(List<TrimbleDealer> dealers) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (TrimbleDealer dealer : dealers) {
            if (dealer == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(dealer, row);
        }
        return workbook;
    }

    static void convertDataToRow(TrimbleDealer store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getAddress());
        Util.createCell(cellNumber++, row, store.getCountry());
        Util.createCell(cellNumber++, row, store.getEmail());
        Util.createCell(cellNumber++, row, store.getLink());
        Util.createCell(cellNumber++, row, store.getPhone());
        Util.createCell(cellNumber++, row, store.getPriorityName());
    }
}
