package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.DjiStore;
import org.example.entity.InterGeoExhibitor;
import org.example.util.Util;

import java.io.IOException;
import java.util.*;

public class InterGeoExhibitorExtractor {
    static final String URL_EXHIBITORS = "https://hinte.brandbox.de/en/es-neo-api-ssl.html?action=el_complete&fairID=1171754&language=en";

    static Map<String, InterGeoExhibitor.Stand> standMap = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        List<InterGeoExhibitor> exhibitors = new ArrayList<>();
        String exhibitorsJsonStr = Util.get(URL_EXHIBITORS);
        JSONObject exhibitorsJson = JSON.parseObject(exhibitorsJsonStr);

        initStandMap(exhibitorsJson);

        JSONArray exhibitorsArr = exhibitorsJson.getJSONObject("lists").getJSONObject("exhibitors").getJSONArray("data");
        for (Object obj : exhibitorsArr) {
            JSONObject eo = (JSONObject) obj;

            InterGeoExhibitor exhibitor = new InterGeoExhibitor();
            exhibitor.setCountry(eo.getString("country"));
            exhibitor.setCity(eo.getString("city"));
            exhibitor.setStreet(eo.getString("street"));

            exhibitor.setTitle(eo.getString("title"));

            JSONArray standsArr = eo.getJSONArray("stands");
            List<InterGeoExhibitor.Stand> stands = new ArrayList<>();
            for (int i = 0; i < standsArr.size(); i++) {
                stands.add(standMap.get(standsArr.getString(i)));
            }

            exhibitor.setStands(stands);
            exhibitor.setEmail(eo.getString("email"));
            exhibitor.setMailContactPerson(eo.getString("mailContactPerson"));
            exhibitor.setDescription(eo.getString("description"));
            exhibitor.setPhone(eo.getString("phone"));
            exhibitor.setWebsite(eo.getString("web"));
            exhibitor.setZipCode(eo.getString("zipcode"));

            exhibitors.add(exhibitor);
        }

        Collections.sort(exhibitors, Comparator.comparing(InterGeoExhibitor::getTitle));

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(exhibitors);
        Util.write(workbook, "result/intergeo-exhibitors.xlsx");
    }

    static void initStandMap(JSONObject rootObject) {
        JSONArray standsArr = rootObject.getJSONArray("stands");
        for (Object obj : standsArr) {
            JSONObject standObj = (JSONObject) obj;
            String id = standObj.getString("id");
            InterGeoExhibitor.Stand stand = new InterGeoExhibitor.Stand(id, standObj.getString("hall"), standObj.getString("booth"));
            standMap.put(id, stand);
        }
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Country", 2500));
        CELL_HEADERS.add(new Util.Header("City", 2000));
        CELL_HEADERS.add(new Util.Header("Street", 2500));

        CELL_HEADERS.add(new Util.Header("Title", 4500));
        CELL_HEADERS.add(new Util.Header("Stands", 5000));

        CELL_HEADERS.add(new Util.Header("Description", 4000));
        CELL_HEADERS.add(new Util.Header("Email", 4500));
        CELL_HEADERS.add(new Util.Header("MailContactPerson", 6500));
        CELL_HEADERS.add(new Util.Header("Phone", 3500));
        CELL_HEADERS.add(new Util.Header("Website", 3500));
        CELL_HEADERS.add(new Util.Header("ZipCode", 3000));
    }

    static Workbook export(List<InterGeoExhibitor> exhibitors) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (InterGeoExhibitor exhibitor : exhibitors) {
            if (exhibitor == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(exhibitor, row);
        }
        return workbook;
    }

    static void convertDataToRow(InterGeoExhibitor exhibitor, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, exhibitor.getCountry());
        Util.createCell(cellNumber++, row, exhibitor.getCity());
        Util.createCell(cellNumber++, row, exhibitor.getStreet());

        Util.createCell(cellNumber++, row, exhibitor.getTitle());
        Util.createCell(cellNumber++, row, exhibitor.stands2Str());
        Util.createCell(cellNumber++, row, exhibitor.getDescription());
        Util.createCell(cellNumber++, row, exhibitor.getEmail());
        Util.createCell(cellNumber++, row, exhibitor.getMailContactPerson());
        Util.createCell(cellNumber++, row, exhibitor.getPhone());
        Util.createCell(cellNumber++, row, exhibitor.getWebsite());
        Util.createCell(cellNumber++, row, exhibitor.getZipCode());
    }
}
