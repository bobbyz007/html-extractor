package org.example.deprecated;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.Store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DjiStoreExtractor {
    static final String URL_CONTINENTS = "https://www-api.dji.com/nz/api/dealers/continents";
    static final String URL_COUNTRIES_BY_CONTINENT = "https://www-api.dji.com/nz/api/dealers/countries/?continent=%s";
    static final String URL_STORES_BY_COUNTRY = "https://www-api.dji.com/nz/api/dealers/stores/%s";

    static final String[] EXCLUDES_COUNTRY = {"cn"};

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private Map<String, String> getContinents() {
        String json = parse(URL_CONTINENTS);
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray dataArr = jsonObject.getJSONArray("data");
        Map<String, String> continentMap = new HashMap<>();

        for (int i = 0; i < dataArr.size(); ++i) {
            JSONObject con = dataArr.getJSONObject(i);
            String code = con.getString("code");
            String name = con.getString("name");
            continentMap.put(code, name);
        }

        return continentMap;
    }

    private Map<String, String> getCountries(String conCode) throws URISyntaxException, MalformedURLException {
        String strUrl = String.format(URL_COUNTRIES_BY_CONTINENT, conCode);
        URL url = new URL(strUrl);
        URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);

        String json = parse(uri);
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray dataArr = jsonObject.getJSONArray("data");
        Map<String, String> countryMap = new HashMap<>();

        for (int i = 0; i < dataArr.size(); ++i) {
            JSONObject con = dataArr.getJSONObject(i);
            String code = con.getString("code");
            String name = con.getString("name");
            countryMap.put(code, name);
        }

        return countryMap;
    }

    private List<Store> getStores(String countryCode, String countryName, String conName) {
        String json = parse(String.format(URL_STORES_BY_COUNTRY, countryCode));
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray dataArr = jsonObject.getJSONArray("data");

        List<Store> stores = new ArrayList<>();
        for (int i = 0; i < dataArr.size(); ++i) {
            JSONObject jsonStore = dataArr.getJSONObject(i);
            Store store = new Store();
            store.setContinent(conName);
            store.setCountry(countryName);
            store.setCity(jsonStore.getString("city"));
            store.setName(jsonStore.getString("name"));
            store.setAddress(jsonStore.getString("address"));
            store.setShopType(jsonStore.getString("shop_type"));
            store.setZipCode(jsonStore.getString("zip_code"));
            store.setEmail(jsonStore.getString("email"));
            store.setOpenHours(jsonStore.getString("open_hours"));
            store.setPhone(jsonStore.getString("phone"));
            stores.add(store);
        }
        return stores;
    }

    private boolean countryIgnored(String country) {
        for (String str : EXCLUDES_COUNTRY) {
            if (str.equals(country)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        DjiStoreExtractor main = new DjiStoreExtractor();

        List<Store> stores = new ArrayList<>();

        Map<String, String> continents = main.getContinents();
        for (Map.Entry<String, String> entry : continents.entrySet()) {
            String conCode = entry.getKey();
            String conName = entry.getValue();

            Map<String, String> countries = main.getCountries(conCode);
            for (Map.Entry<String, String> countryEntry : countries.entrySet()) {
                String countryCode = countryEntry.getKey();
                String countryName = countryEntry.getValue();

                if (main.countryIgnored(countryCode)) {
                    continue;
                }
                stores.addAll(main.getStores(countryCode, countryName, conName));
            }
        }

        main.writeFile("result/result.xlsx", stores);
    }

    private void writeFile(String filename, List<Store> stores) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        int rowCnt = 0;
        for (Store store : stores) {
            XSSFRow row = sheet.createRow(rowCnt++);
            row.createCell(0).setCellValue(store.getContinent());
            row.createCell(1).setCellValue(store.getCountry());
            row.createCell(2).setCellValue(store.getCity());
            row.createCell(3).setCellValue(store.getName());
            row.createCell(4).setCellValue(store.getAddress());
            row.createCell(5).setCellValue(store.getShopType());
            row.createCell(6).setCellValue(store.getZipCode());
            row.createCell(7).setCellValue(store.getEmail());
            row.createCell(8).setCellValue(store.getOpenHours());
            row.createCell(9).setCellValue(store.getPhone());
        }

        File outFile = new File(filename);
        try (OutputStream outputStream = new FileOutputStream(outFile)){
            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parse(URI uri) {
        try {
            CloseableHttpResponse response = httpClient.execute(new HttpGet(uri));
            // System.out.println("status: " + response.getStatusLine());

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (IOException e) {
            System.err.println("Error in getting: " + uri + " as " + e.getMessage());
        }
        return null;
    }

    private String parse(String url) {
        return parse(URI.create(url));
    }
}
