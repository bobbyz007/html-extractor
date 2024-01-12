package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.EcoFlowStore;
import org.example.entity.SurveyMember;
import org.example.util.Util;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class EcoFlowRetailStoreExtractor {
    static final int FETCH_TIMEOUT_MILLIS = 60 * 1000;

    static final String[] COUNTRIES = {"US", "JP", "AU"};

    static final String URL_TPL = "https://api.ecoflow.com/website/storeAddress/list?country=%s&addressEnable=true";

    public static void main(String[] args) throws IOException {
        List<EcoFlowStore> results = fetchStores();

        Workbook workbook = export(results);
        Util.write(workbook, "result/ecoflow-retail-stores.xlsx");
    }

    static List<EcoFlowStore> fetchStores() {
        List<EcoFlowStore> results = new ArrayList<>();
        for (String country : COUNTRIES) {
            System.out.println(String.format("Start fetching for country: %s", country));
            String url = String.format(URL_TPL, country);
            List<EcoFlowStore> result = fetchStore(url);
            results.addAll(result);
        }
        return results;
    }

    static List<EcoFlowStore> fetchStore(String url) {
        String jsonBody;
        try {
            jsonBody = Jsoup.connect(url).ignoreContentType(true).timeout(FETCH_TIMEOUT_MILLIS)
                    .execute().body();
        } catch (Exception ex) {
            System.err.println("fetching timeout error: " + url);
            return null;
        }

        Object obj = JSON.parse(jsonBody);
        if (obj == null || !(obj instanceof JSONObject) || !(((JSONObject) obj).get("data") instanceof JSONArray)) {
            return null;
        }
        List<EcoFlowStore> resultList = new ArrayList<>();
        JSONArray jsonArray = ((JSONObject) obj).getJSONArray("data");
        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            EcoFlowStore store = new EcoFlowStore();
            store.setName(jsonObject.getString("name"));
            store.setAddress(jsonObject.getString("address"));
            store.setCity(jsonObject.getString("city"));
            store.setState(jsonObject.getString("state"));
            store.setCountry(jsonObject.getString("country"));
            store.setPhone(jsonObject.getString("phone"));
            store.setType(jsonObject.getString("type"));
            store.setWebLink(jsonObject.getString("webLink"));

            resultList.add(store);
        }
        return resultList;
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Name", 5000));
        CELL_HEADERS.add(new Util.Header("Address", 10000));
        CELL_HEADERS.add(new Util.Header("City", 3000));
        CELL_HEADERS.add(new Util.Header("State", 2000));
        CELL_HEADERS.add(new Util.Header("Country", 3000));
        CELL_HEADERS.add(new Util.Header("Phone", 4000));
        CELL_HEADERS.add(new Util.Header("Type", 3000));
        CELL_HEADERS.add(new Util.Header("WebLink", 7000));
    }

    static Workbook export(List<EcoFlowStore> stores) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (EcoFlowStore store : stores) {
            if (store == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(store, row);
        }
        return workbook;
    }

    static void convertDataToRow(EcoFlowStore store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getAddress());
        Util.createCell(cellNumber++, row, store.getCity());
        Util.createCell(cellNumber++, row, store.getState());
        Util.createCell(cellNumber++, row, store.getCountry());
        Util.createCell(cellNumber++, row, store.getPhone());
        Util.createCell(cellNumber++, row, store.getType());
        Util.createCell(cellNumber++, row, store.getWebLink());
    }
}
