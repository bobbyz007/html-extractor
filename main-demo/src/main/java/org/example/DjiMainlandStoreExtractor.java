package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.DjiStore;
import org.example.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DjiMainlandStoreExtractor {
    static final String URL_STORES = "https://www-api.dji.com/nz/api/dealers/stores/cn";

    public static void main(String[] args) throws IOException, InterruptedException {
        List<DjiStore> stores = new ArrayList<>();
        String storesJsonStr = Util.get(URL_STORES);
        JSONObject storesJson = JSON.parseObject(storesJsonStr);
        JSONArray storesArr = storesJson.getJSONArray("data");
        for (Object obj : storesArr) {
            JSONObject storeObj = (JSONObject) obj;
            String city = storeObj.getString("city");
            String name = storeObj.getString("name");
            String address=  storeObj.getString("address");
            String openHours = storeObj.getString("open_hours");
            String phone = storeObj.getString("phone");
            String email = storeObj.getString("email");

            DjiStore s = new DjiStore(city, name, address, openHours, phone, email);

            stores.add(s);
        }

        Collections.sort(stores, Comparator.comparing(DjiStore::getCity));

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(stores);
        Util.write(workbook, "dji-mainland-stores.xlsx");
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("City", 2500));
        CELL_HEADERS.add(new Util.Header("Name", 7000));
        CELL_HEADERS.add(new Util.Header("Address", 15000));
        CELL_HEADERS.add(new Util.Header("Open hours", 5000));
        CELL_HEADERS.add(new Util.Header("Phone", 6000));
        CELL_HEADERS.add(new Util.Header("Email", 6000));
    }

    static Workbook export(List<DjiStore> stores) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (DjiStore store : stores) {
            if (store == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(store, row);
        }
        return workbook;
    }

    static void convertDataToRow(DjiStore store, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, store.getCity());
        Util.createCell(cellNumber++, row, store.getName());
        Util.createCell(cellNumber++, row, store.getAddress());
        Util.createCell(cellNumber++, row, store.getOpenHours());
        Util.createCell(cellNumber++, row, store.getPhone());
        Util.createCell(cellNumber++, row, store.getEmail());
    }
}
