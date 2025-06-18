package org.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.example.entity.EpisodeContent;
import org.example.entity.EpisodeInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoxLittleExtractor {
    static final int TIMEOUT_MILLIS = 30 * 1000;
    static final String DIR_PREFIX_OUTPUT = "D:\\workspace\\english\\littlefox\\text\\level";
    static final String URL_FULL_LIST = "https://www.littlefox.com/cn/full_list";
    static final String URL_PREFIX_TEXT = "https://www.littlefox.com/cn/supplement/org/";

    static Map<Integer, Map<String, List<EpisodeInfo>>> levelEpisodeMap = null;
    static final int level = 1;

    public static void main(String[] args) throws IOException, InterruptedException {
        levelEpisodeMap = fetchlevelMap(URL_FULL_LIST);

        // https://www.cnblogs.com/Dreamer-1/p/10469430.html
        export(level);
    }

    public static String sanitizeWithCommons(String name) {
        return StringUtils.replaceChars(name, "\\/:*?\"<>|", "____ ____");
    }

    static EpisodeContent fetchEipsodeContent(String episodeId) throws IOException {
        System.out.println("Starting fetch episode: " + episodeId);

        Document document = Jsoup.connect(URL_PREFIX_TEXT + episodeId).timeout(TIMEOUT_MILLIS).get();
        Element contentEle = document.selectFirst("div.original_cont_box");
        String episodeName = contentEle.selectFirst("dl > dt").text();
        Elements paraEles = contentEle.select("dl > dd > div");
        EpisodeContent episodeContent = new EpisodeContent(episodeName, new ArrayList<>());
        for (Element paraEle : paraEles) {
            episodeContent.getParagraphs().add(paraEle.text());
        }

        return episodeContent;
    }

    static Map<Integer, Map<String, List<EpisodeInfo>>> fetchlevelMap(String url) throws IOException {
        Document document = Jsoup.connect(url).timeout(TIMEOUT_MILLIS).get();
        Elements allEles = document.select("div#story_body_area > div");
        int level = 1;
        Map<Integer, Map<String, List<EpisodeInfo>>> levelEpisodeMap =new HashMap<>();
        for (Element divEle : allEles) {
            String clazzAttr = divEle.attr("class");
            if (StringUtils.equals(clazzAttr, "full_list_level")) {
                String idAttr = divEle.attr("id");
                level = Integer.valueOf(idAttr.substring(idAttr.indexOf("_") + 1, idAttr.lastIndexOf("_")));

                levelEpisodeMap.computeIfAbsent(level, k -> new HashMap<>());

            } else if (StringUtils.equals(clazzAttr, "full_list_cont")) {
                // here are div.full_list_cont
                String movieName = divEle.selectFirst("span.full_list_cont_tit > strong").text();
                List<EpisodeInfo> episodeList = levelEpisodeMap.get(level).computeIfAbsent(movieName, k -> new ArrayList<>());
                for (Element liEle : divEle.select("div.full_list_cont_list > ul > li")) {
                    Element aEle = liEle.selectFirst("a");
                    if (aEle == null) {
                        continue;
                    }
                    String episodeName = aEle.text();
                    String onclick = aEle.attr("onclick");
                    String idPart = onclick.substring(onclick.indexOf(",") + 1, onclick.lastIndexOf(",")).trim();
                    String id = idPart.substring(idPart.indexOf("'") + 1, idPart.lastIndexOf("'"));
                    episodeList.add(new EpisodeInfo(id, episodeName));
                }
            }
        }
        return levelEpisodeMap;
    }

    static void export(Integer level) throws IOException {
        Map<String, List<EpisodeInfo>> episodeMap = levelEpisodeMap.get(level);
        File outputDir = new File(DIR_PREFIX_OUTPUT + level);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        for (Map.Entry<String, List<EpisodeInfo>> entry : episodeMap.entrySet()) {
            String movieName = sanitizeWithCommons(entry.getKey());
            // 创建一个新文档
            XWPFDocument doc = new XWPFDocument();
            System.out.println("Starting fetch movie: " + movieName);
            for (EpisodeInfo episodeInfo : entry.getValue()) {
                EpisodeContent episodeContent = null;
                final int tryoutTimes = 3;
                for (int i = 0; i < tryoutTimes; i++) {
                    try {
                        episodeContent = fetchEipsodeContent(episodeInfo.getId());
                        break;
                    } catch (Exception ex) {
                        try {
                            System.out.println("Fetch exception, sleep a while and try again: " + (i + 1));
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            //just ignore
                        }
                    }
                }
                if (episodeContent == null) {
                    throw new IOException("Tried but failed.");
                }

                createHeading(doc, episodeContent.getTitle(), 16, true);
                for (String paragraph : episodeContent.getParagraphs()) {
                    createParagraph(doc, paragraph);
                }
                createParagraph(doc, ""); // add a blank paragraph as separator between episodes
            }

            // 保存文件到本地
            try (FileOutputStream out = new FileOutputStream(new File(outputDir, movieName + ".docx"))) {
                doc.write(out);
            }
        }
    }

    /**
     * 创建标题
     */
    private static void createHeading(XWPFDocument doc, String text, int fontSize, boolean isBold) {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Times New Roman");  // 设置字体
        run.setFontSize(fontSize); // 字号
        run.setBold(isBold);      // 加粗
    }

    /**
     * 创建普通段落
     */
    private static void createParagraph(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);       // 默认字号
    }

    /**
     * 创建带格式的段落（如斜体、颜色）
     */
    private static void createFormattedParagraph(XWPFDocument doc, String text, boolean isItalic, boolean isColored) {
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setItalic(isItalic);   // 斜体
        if (isColored) {
            run.setColor("FF0000"); // 红色（Hex 格式）
        }
    }
}

