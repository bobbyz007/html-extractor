package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.DjiAgent;
import org.example.util.Util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DjiEnterpriseDealersExtractor {
    // static final String TPL_DEALER_URL = "https://www-api.dji.com/api/where-to-buy/agriculture-dealer-maps?area=%s";
    static final String TPL_DEALER_URL = "https://www-api.dji.com/nz/api/where-to-buy/dealer-maps?area=%s";

    // static final String TPL_COUNTRY_URL = "https://www-api.dji.com/api/dealers/countries/?continent=%s";
    static final String TPL_COUNTRY_URL = "https://www-api.dji.com/nz/api/dealers/countries/?continent=%s";
    static final String[] CONTINENTS = {"Africa", "Asia", "Australia", "Europe", "North America", "South America", "Middle East and Africa"};

    public static void main(String[] args) throws IOException, InterruptedException {
        List<DjiAgent> agents = new ArrayList<>();
        for (String continent : CONTINENTS) {
            String countryStr = Util.get(String.format(TPL_COUNTRY_URL, URLEncoder.encode(continent, "UTF-8")));

            JSONObject countryJson = JSON.parseObject(countryStr);
            JSONArray storesArr = countryJson.getJSONArray("data");
            for (Object obj : storesArr) {
                JSONObject countryObj = (JSONObject) obj;
                String code = countryObj.getString("code");
                String countryName = countryObj.getString("name");

                // exclude china
                if (StringUtils.equals(code, "cn")) {
                    continue;
                }
                String agentStr = Util.get(String.format(TPL_DEALER_URL, code));
                JSONObject agentsJson = JSON.parseObject(agentStr);
                JSONArray areaArr = agentsJson.getJSONArray("data");
                if (areaArr.size() == 0) {
                    System.err.println("No enterprise dealers for " + countryName);
                    continue;
                }
                JSONObject areaObj = areaArr.getJSONObject(0);
                JSONArray agentArr = areaObj.getJSONArray("distributors");

                for (Object obj2 : agentArr) {
                    JSONObject agentObj = (JSONObject) obj2;
                    DjiAgent djiAgent = new DjiAgent(agentObj.getString("area"), agentObj.getString("country_name"),
                            agentObj.getString("state"), agentObj.getString("city"),
                            agentObj.getString("name"), agentObj.getString("website"), agentObj.getString("address"),
                            agentObj.getString("tel"), agentObj.getString("email"));

                    agents.add(djiAgent);
                }
            }
        }

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(agents);
        Util.write(workbook, "result/dji-enterprise-dealers.xlsx");
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Continent", 1500));
        CELL_HEADERS.add(new Util.Header("Country", 2500));
        CELL_HEADERS.add(new Util.Header("State", 2500));
        CELL_HEADERS.add(new Util.Header("City", 2500));
        CELL_HEADERS.add(new Util.Header("Name", 7000));
        CELL_HEADERS.add(new Util.Header("Website", 6000));
        CELL_HEADERS.add(new Util.Header("Address", 12000));
        CELL_HEADERS.add(new Util.Header("Phone", 6000));
        CELL_HEADERS.add(new Util.Header("Email", 6000));
    }

    static Workbook export(List<DjiAgent> agents) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (DjiAgent agent : agents) {
            if (agent == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(agent, row);
        }
        return workbook;
    }

    static void convertDataToRow(DjiAgent agent, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, agent.getContinent());
        Util.createCell(cellNumber++, row, agent.getCountry());
        Util.createCell(cellNumber++, row, agent.getState());
        Util.createCell(cellNumber++, row, agent.getCity());
        Util.createCell(cellNumber++, row, agent.getName());
        Util.createCell(cellNumber++, row, agent.getWebsite());
        Util.createCell(cellNumber++, row, agent.getAddress());
        Util.createCell(cellNumber++, row, agent.getPhone());
        Util.createCell(cellNumber++, row, agent.getEmail());
    }
}
