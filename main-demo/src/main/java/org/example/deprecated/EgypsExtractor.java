package org.example.deprecated;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.Exhibitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EgypsExtractor {
    static final String URL_EXHIBITORS = "https://marketingmanual.egyps.com/umbraco/surface/ExhibitorList/GetAllExhibitors";

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private List<Exhibitor> getExhibitors() throws IOException {
        String json = parse(URL_EXHIBITORS, true);
        JSONArray dataArr = JSON.parseArray(json);

        List<Exhibitor> exhibitors = new ArrayList<>();
        for (int i = 0; i < dataArr.size(); ++i) {
            JSONObject jsonStore = dataArr.getJSONObject(i);
            Exhibitor exhibitor = new Exhibitor();

            exhibitor.setAddress(jsonStore.getString("Address"));
            exhibitor.setCompanyName(jsonStore.getString("CompanyName"));
            exhibitor.setDescription(jsonStore.getString("Description"));
            exhibitor.setEmail(jsonStore.getString("Email"));
            exhibitor.setFax(jsonStore.getString("Fax"));
            exhibitor.setPhoneNumber(jsonStore.getString("PhoneNumber"));
            exhibitor.setWebsiteLink(jsonStore.getString("WebsiteLink"));
            exhibitor.setCountryName(jsonStore.getString("CountryName"));

            exhibitors.add(exhibitor);
        }
        return exhibitors;
    }

    public static void main(String[] args) throws Exception {
        EgypsExtractor main = new EgypsExtractor();

        List<Exhibitor> exhibitors = main.getExhibitors();

        main.writeFile("result/result_egyps.xlsx", exhibitors);
    }

    private void writeFile(String filename, List<Exhibitor> exhibitors) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        int rowCnt = 0;
        for (Exhibitor exhibitor : exhibitors) {
            XSSFRow row = sheet.createRow(rowCnt++);
            row.createCell(0).setCellValue(exhibitor.getCompanyName());
            row.createCell(1).setCellValue(exhibitor.getDescription());
            row.createCell(2).setCellValue(exhibitor.getAddress());
            row.createCell(3).setCellValue(exhibitor.getEmail());
            row.createCell(4).setCellValue(exhibitor.getFax());
            row.createCell(5).setCellValue(exhibitor.getPhoneNumber());
            row.createCell(6).setCellValue(exhibitor.getWebsiteLink());
            row.createCell(7).setCellValue(exhibitor.getCountryName());
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

    private String parse(String url, boolean fromFile) throws IOException {
        return fromFile ? FileUtils.readFileToString(new File("D:\\workspace\\web-extractor\\GetAllExhibitors.json"), Charset.forName("UTF-8")) : parse(URI.create(url));
    }
}
