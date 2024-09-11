package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.DjiStore;
import org.example.entity.GasTechEvent;
import org.example.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GasTechEventExtractor {
    static final String URL_EVENTS = "https://api.futureenergyasia.com/umbraco/surface/ExhibitorList/GetAllExhibitorsGastech";

    public static void main(String[] args) throws IOException, InterruptedException {
        List<GasTechEvent> stores = new ArrayList<>();
        String eventsJsonStr = Util.get(URL_EVENTS);
        JSONArray eventsArr = JSON.parseArray(eventsJsonStr);
        for (Object obj : eventsArr) {
            JSONObject storeObj = (JSONObject) obj;
            String companyName = storeObj.getString("CompanyName");
            String standNumber = storeObj.getString("StandNumber");
            String countryName = storeObj.getString("CountryName");
            String address = storeObj.getString("Address");
            String websiteLink = storeObj.getString("WebsiteLink");
            String description = storeObj.getString("Description");

            GasTechEvent s = new GasTechEvent(companyName, standNumber, countryName, address, websiteLink, description);

            stores.add(s);
        }

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(stores);
        Util.write(workbook, "result/gas-tech-events.xlsx");
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Company", 5000));
        CELL_HEADERS.add(new Util.Header("Stand", 2500));
        CELL_HEADERS.add(new Util.Header("Country", 3000));
        CELL_HEADERS.add(new Util.Header("Address", 6000));
        CELL_HEADERS.add(new Util.Header("Website", 6000));
        CELL_HEADERS.add(new Util.Header("Description", 10000));
    }

    static Workbook export(List<GasTechEvent> events) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (GasTechEvent event : events) {
            if (event == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(event, row);
        }
        return workbook;
    }

    static void convertDataToRow(GasTechEvent store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getCompanyName());
        Util.createCell(cellNumber++, row, store.getStandNumber());
        Util.createCell(cellNumber++, row, store.getCountryName());
        Util.createCell(cellNumber++, row, store.getAddress());
        Util.createCell(cellNumber++, row, store.getWebsiteLink());
        Util.createCell(cellNumber++, row, store.getDescription());
    }
}
