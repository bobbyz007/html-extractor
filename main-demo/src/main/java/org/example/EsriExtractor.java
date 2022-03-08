package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.Partner;
import org.example.util.Util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsriExtractor {
    static final String URL = "https://esearchapi.esri.com/search";

    static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    static final int NUM = 15;

    static Map<String, Object> INIT_REQUEST_MAP = initRequestMap();

    public static void main(String[] args) throws IOException {
        boolean finished = false;
        int start = 0;
        List<Partner> resultList = new ArrayList<>();

        do {
            String requestJson = requestJson(start);
            String response = post(URL, requestJson);

            JSONObject jsonObj = JSON.parseObject(response);
            JSONObject searchObj = jsonObj.getJSONObject("search");
            int resultCount = searchObj.getInteger("count");

            JSONArray hits = searchObj.getJSONArray("hits");
            for (Object obj : hits) {
                JSONObject hitObj = (JSONObject) obj;

                Partner partner = new Partner();
                resultList.add(partner);

                JSONObject docObj = hitObj.getJSONObject("doc");
                JSONObject metaFieldsObj = docObj.getJSONObject("metaFields");
                partner.setCompanyName(metaFieldsObj.getString("companyName"));
                partner.setDescription(docObj.getString("description"));
                partner.setSpecialities(metaFieldsObj.getString("specialty"));
                partner.setSolutionType(metaFieldsObj.getString("solution-type"));
                partner.setPartnerTier(metaFieldsObj.getString("partner-tier"));

                Object serviceObj = metaFieldsObj.get("services");
                partner.setServices(serviceObj != null ? serviceObj.toString() : null);
                partner.setBusinessClassification(metaFieldsObj.getString("business-classification"));
                Object locationObj = metaFieldsObj.get("location");
                partner.setLocation(locationObj != null ? locationObj.toString() : null);

                String content = docObj.getString("content");
                int contactIndex = content.lastIndexOf("Contact us");
                if (contactIndex != -1) {
                    String subContact = content.substring(contactIndex);
                    int programIndex = subContact.indexOf("                Program");
                    if (programIndex != -1) {
                        partner.setContact(subContact.substring(0, programIndex).trim());
                    }
                }
            }

            finished = (start + NUM) >= resultCount;

            // for next request
            start += NUM;
        } while (!finished);

        Workbook workbook = export(resultList);
        Util.write(workbook, "/home/justin/workspace/html-extractor/result/partner-result.xlsx");
    }

    static Map<String, Object> initRequestMap() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("lr", "en");
        requestData.put("num", NUM);
        requestData.put("client", "esri_explore");
        requestData.put("start", 0);
        requestData.put("partialfields", "page-type:partners");
        requestData.put("q", "");
        requestData.put("site", "esri_all_partners");
        requestData.put("format", "json");
        requestData.put("len", 100);
        requestData.put("offset", 0);
        requestData.put("sort", "metaFields.company-sort.keyword:A");
        return requestData;
    }

    static String requestJson(int start) {
        INIT_REQUEST_MAP.put("start", start);
        return JSON.toJSONString(INIT_REQUEST_MAP);
    }

    static String post(String url, String jsonParams) throws IOException {
        HttpPost post = new HttpPost(url);
        // 建立一个NameValuePair数组，用于存储欲传送的参数
        post.addHeader("Content-type","application/json; charset=utf-8");
        post.setHeader("Accept", "application/json");
        post.setEntity(new StringEntity(jsonParams, Charset.forName("UTF-8")));

        HttpResponse response = httpClient.execute(post);
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
        CELL_HEADERS.add(new Util.Header("Company Name", 4000));
        CELL_HEADERS.add(new Util.Header("Description", 4000));
        CELL_HEADERS.add(new Util.Header("Specialty", 4000));
        CELL_HEADERS.add(new Util.Header("Solution Type", 4000));
        CELL_HEADERS.add(new Util.Header("Partner Tier", 4000));
        CELL_HEADERS.add(new Util.Header("Services", 4000));
        CELL_HEADERS.add(new Util.Header("Business Classification", 4000));
        CELL_HEADERS.add(new Util.Header("Location", 4000));
        CELL_HEADERS.add(new Util.Header("Contact", 9000));
    }

    static Workbook export(List<Partner> partners) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (Partner partner : partners) {
            if (partner == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(partner, row);
        }
        return workbook;
    }

    static void convertDataToRow(Partner partner, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, partner.getCompanyName());
        Util.createCell(cellNumber++, row, partner.getDescription());
        Util.createCell(cellNumber++, row, partner.getSpecialities());
        Util.createCell(cellNumber++, row, partner.getSolutionType());

        Util.createCell(cellNumber++, row, partner.getPartnerTier());

        Util.createCell(cellNumber++, row, partner.getServices());
        Util.createCell(cellNumber++, row, partner.getBusinessClassification());
        Util.createCell(cellNumber++, row, partner.getLocation());
        Util.createCell(cellNumber++, row, partner.getContact());
    }
}
