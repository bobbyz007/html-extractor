package org.example;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.SurveyMember;
import org.example.util.Util;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 澳大利亚纬度范围：南纬10°41'-43°39'之间； 经度范围：东经112°-154°之间。
 * https://surveying.org.au/members/
 */
public class SurveyingMemberExtractor {
    static final int FETCH_TIMEOUT_MILLIS = 60 * 1000;
    /*// 经度
    static final double LNG_MIN = 120; // 112
    static final double LNG_MAX = 154; // 154
    static final double LNG_STEP = 0.5;
    // 纬度
    static final double LAT_MIN = -10.41; // -10.41
    static final double LAT_MAX = -43.39; // -43.39
    static final double LAT_STEP = -0.5;*/

    // 优化经度
    static final double LNG_MIN = 112;
    static final double LNG_MAX = 155; // 154
    static final double LNG_STEP = 0.3;
    // 优化纬度
    static final double LAT_MIN = -10.41; // -10.41
    static final double LAT_MAX = -44; // -43.39
    static final double LAT_STEP = -0.3;

    static final String URL_TPL = "https://surveying.org.au/wp-admin/admin-ajax.php?action=store_search&lng=%s&lat=%s&max_results=25&search_radius=50";

    public static void main(String[] args) throws IOException {
        Set<SurveyMember> results = fetchSurveyMembers();

        Workbook workbook = export(results);
        Util.write(workbook, "result/surveying-member.xlsx");
    }

    static Set<SurveyMember> fetchSurveyMembers() throws IOException {
        Set<SurveyMember> results = new HashSet<>();
        int mayDuplicateTotal = 0;
        for (double lng = LNG_MIN; lng <= LNG_MAX; lng += LNG_STEP) {
            for (double lat = LAT_MIN; lat >= LAT_MAX; lat += LAT_STEP) {
                System.out.println(String.format("Start fetching for longitude: %s, latitude: %s", lng, lat));
                String url = String.format(URL_TPL, lng, lat);
                List<SurveyMember> result = fetchSurveyMember(url);
                if (result != null && result.size() > 0) {
                    results.addAll(result);
                    mayDuplicateTotal += result.size();
                    System.out.println("End fetching successfully with result number: " + result.size());
                } else {
                    System.out.println("End fetching with empty result: may be erroneous");
                }
            }
        }

        System.out.println(String.format("maybe duplicate/set: %s / %s", mayDuplicateTotal, results.size()));
        return results;
    }

    static List<SurveyMember> fetchSurveyMember(String url) {
        String jsonBody;
        try {
            jsonBody = Jsoup.connect(url).ignoreContentType(true).timeout(FETCH_TIMEOUT_MILLIS)
                    .execute().body();
        } catch (Exception ex) {
            System.err.println("fetching timeout error: " + url);
            return null;
        }

        Object obj = JSON.parse(jsonBody);
        if (obj == null || !(obj instanceof JSONArray)) {
            return null;
        }
        List<SurveyMember> resultList = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) obj;
        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            SurveyMember member = new SurveyMember();
            member.setAddress(jsonObject.getString("address"));
            member.setAddress2(jsonObject.getString("address2"));
            member.setCity(jsonObject.getString("city"));
            member.setCountry(jsonObject.getString("country"));
            member.setDistance(jsonObject.getString("distance"));
            member.setEmail(jsonObject.getString("email"));
            member.setFax(jsonObject.getString("fax"));
            member.setHours(jsonObject.getString("hours"));
            member.setId(jsonObject.getString("id"));
            member.setLat(jsonObject.getString("lat"));
            member.setLng(jsonObject.getString("lng"));
            member.setPermalink(jsonObject.getString("permalink"));
            member.setPhone(jsonObject.getString("phone"));
            member.setState(jsonObject.getString("state"));
            member.setStore(jsonObject.getString("store"));
            member.setUrl(jsonObject.getString("url"));
            member.setZip(jsonObject.getString("zip"));

            resultList.add(member);
        }
        return resultList;
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Id", 2000));
        CELL_HEADERS.add(new Util.Header("Store", 4000));
        CELL_HEADERS.add(new Util.Header("Address", 4000));
        CELL_HEADERS.add(new Util.Header("Address2", 4000));
        CELL_HEADERS.add(new Util.Header("State", 3200));
        CELL_HEADERS.add(new Util.Header("City", 4000));
        CELL_HEADERS.add(new Util.Header("Country", 4000));
        CELL_HEADERS.add(new Util.Header("Zip", 2000));
        CELL_HEADERS.add(new Util.Header("PHone", 4000));
        CELL_HEADERS.add(new Util.Header("Fax", 4000));
        CELL_HEADERS.add(new Util.Header("Email", 4000));
        CELL_HEADERS.add(new Util.Header("Permanent Link", 5300));
        CELL_HEADERS.add(new Util.Header("Url", 4000));
    }

    static Workbook export(Set<SurveyMember> members) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (SurveyMember member : members) {
            if (member == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(member, row);
        }
        return workbook;
    }

    static void convertDataToRow(SurveyMember member, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, member.getId());
        Util.createCell(cellNumber++, row, member.getStore());
        Util.createCell(cellNumber++, row, member.getAddress());
        Util.createCell(cellNumber++, row, member.getAddress2());
        Util.createCell(cellNumber++, row, member.getState());
        Util.createCell(cellNumber++, row, member.getCity());
        Util.createCell(cellNumber++, row, member.getCountry());
        Util.createCell(cellNumber++, row, member.getZip());
        Util.createCell(cellNumber++, row, member.getPhone());
        Util.createCell(cellNumber++, row, member.getFax());
        Util.createCell(cellNumber++, row, member.getEmail());
        Util.createCell(cellNumber++, row, member.getPermalink());
        Util.createCell(cellNumber++, row, member.getUrl());
    }
}
