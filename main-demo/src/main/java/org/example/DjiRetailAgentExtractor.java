package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.DjiRetailStore;
import org.example.util.Util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DjiRetailAgentExtractor {
    static final String TPL_STATE_URL = "https://www-api.dji.com/nz/api/dealers/states/%s";
    static final String TPL_AGENT_URL = "https://www-api.dji.com/nz/api/dealers/stores/%s";
    static final String TPL_COUNTRY_URL = "https://www-api.dji.com/api/dealers/countries/?continent=%s";

    // FOR TESTING
    //static final String[] CONTINENTS = {"North America"};
    static final String[] CONTINENTS = {"Africa", "Asia", "Australia", "Europe", "North America", "South America", "Middle East and Africa"};

    public static void main(String[] args) throws IOException, InterruptedException {
        List<DjiRetailStore> agents = new ArrayList<>();
        for (String continent : CONTINENTS) {
            String countryStr = Util.get(String.format(TPL_COUNTRY_URL, URLEncoder.encode(continent, "UTF-8")));

            JSONObject countryJson = JSON.parseObject(countryStr);
            JSONArray countryArr = countryJson.getJSONArray("data");
            for (Object obj : countryArr) {
                JSONObject countryObj = (JSONObject) obj;
                String countryCode = countryObj.getString("code");
                String countryName = countryObj.getString("name");

                // exclude china
                if (StringUtils.equals(countryCode, "cn")) {
                    continue;
                }
                String agentStr = Util.get(String.format(TPL_AGENT_URL, countryCode));
                JSONObject agentsJson = JSON.parseObject(agentStr);
                // 需要进一步按照state过滤
                if (agentsJson.getBoolean("state_required")) {
                    String stateStr = Util.get(String.format(TPL_STATE_URL, countryCode));
                    JSONObject stateJson = JSON.parseObject(stateStr);
                    JSONArray stateArr = stateJson.getJSONArray("data");
                    for (Object obj2 : stateArr) {
                        JSONObject stateObj = (JSONObject) obj2;
                        String stateCode = stateObj.getString("code");
                        String stateName = stateObj.getString("name");
                        String agentStrOfState = Util.get(String.format(TPL_AGENT_URL + "/?state=%s", countryCode, stateCode));

                        processStore(continent, countryName, stateName, JSON.parseObject(agentStrOfState), agents);
                    }
                } else {
                    processStore(continent, countryName, "", agentsJson, agents);
                }
            }
        }

        // output result members
        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        Workbook workbook = export(agents);
        Util.write(workbook, "result/dji-retail-stores.xlsx");
    }

    static void processStore(String continent, String countryName, String stateName, JSONObject agentsJson, List<DjiRetailStore> results) {
        JSONArray dataArr = agentsJson.getJSONArray("data");
        if (dataArr.size() == 0) {
            System.err.println("No data for ");
            return;
        }
        for (Object obj2 : dataArr) {
            JSONObject agentObj = (JSONObject) obj2;
            DjiRetailStore djiAgent = DjiRetailStore.builder()
                    .continent(continent).country(countryName)
                    .state(stateName).city(agentObj.getString("city"))
                    .name(agentObj.getString("name")).address(agentObj.getString("address"))
                    .phone(agentObj.getString("phone")).email(agentObj.getString("email"))
                    .shopType(agentObj.getString("shop_type"))
                    .build();

            results.add(djiAgent);
        }
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Continent", 2500));
        CELL_HEADERS.add(new Util.Header("Country", 2500));
        CELL_HEADERS.add(new Util.Header("State", 2500));
        CELL_HEADERS.add(new Util.Header("City", 2500));
        CELL_HEADERS.add(new Util.Header("Name", 7000));
        CELL_HEADERS.add(new Util.Header("Shop Type", 1500));
        CELL_HEADERS.add(new Util.Header("Address", 12000));
        CELL_HEADERS.add(new Util.Header("Phone", 6000));
        CELL_HEADERS.add(new Util.Header("Email", 6000));
    }

    static Workbook export(List<DjiRetailStore> agents) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (DjiRetailStore agent : agents) {
            if (agent == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(agent, row);
        }
        return workbook;
    }

    static void convertDataToRow(DjiRetailStore agent, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, agent.getContinent());
        Util.createCell(cellNumber++, row, agent.getCountry());
        Util.createCell(cellNumber++, row, agent.getState());
        Util.createCell(cellNumber++, row, agent.getCity());
        Util.createCell(cellNumber++, row, agent.getName());
        Util.createCell(cellNumber++, row, agent.getShopType());
        Util.createCell(cellNumber++, row, agent.getAddress());
        Util.createCell(cellNumber++, row, agent.getPhone());
        Util.createCell(cellNumber++, row, agent.getEmail());
    }
}
