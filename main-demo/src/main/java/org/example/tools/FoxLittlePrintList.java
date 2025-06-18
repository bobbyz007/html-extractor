package org.example.tools;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FoxLittlePrintList {
    static final String WORKSPACE_FILE = "C:\\Users\\Justin\\Desktop\\workspace.txt";

    public static void main(String[] args) throws IOException {
        parse();
    }

    static void parse() throws IOException {
        String content = FileUtils.readFileToString(new File(WORKSPACE_FILE), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(content);
        Element divEle = doc.selectFirst("div.full_list_cont");
        // here are div.full_list_cont
        String movieName = divEle.selectFirst("span.full_list_cont_tit > strong").text();
        int serialNo = 1;
        for (Element liEle : divEle.select("div.full_list_cont_list > ul > li")) {
            Element aEle = liEle.selectFirst("a");
            if (aEle == null) {
                continue;
            }
            String episodeName = aEle.text().trim();
            String onclick = aEle.attr("onclick");
            String idPart = onclick.substring(onclick.indexOf(",") + 1, onclick.lastIndexOf(",")).trim();
            String id = idPart.substring(idPart.indexOf("'") + 1, idPart.lastIndexOf("'"));
            // String.format("%03d ", serialNo++);
            int idx = episodeName.indexOf(".");
            if (idx != -1) {
                episodeName = episodeName.substring(idx + 1, episodeName.length()).trim();
            }
            System.out.println(String.format("%03d ", serialNo++) + episodeName + ",");
        }
    }
}
