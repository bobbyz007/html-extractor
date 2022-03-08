package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.example.entity.TopPositioningDealer;
import org.example.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopPositionExtractor {
    static final String HTML_COUNTRY = "<select class=\"form-control dark country form-control form-select\" id=\"dealerCountry\" name=\"country\"><option value=\"Albania\">Albania</option><option value=\"Angola\">Angola</option><option value=\"Argentina\">Argentina</option><option value=\"Australia\">Australia</option><option value=\"Austria\">Austria</option><option value=\"Belarus\">Belarus</option><option value=\"Belgium\">Belgium</option><option value=\"Bolivia\">Bolivia</option><option value=\"Bosnia and Herzegovina\">Bosnia and Herzegovina</option><option value=\"Brazil\">Brazil</option><option value=\"Bulgaria\">Bulgaria</option><option value=\"Canada\">Canada</option><option value=\"Chile\">Chile</option><option value=\"China\">China</option><option value=\"Colombia\">Colombia</option><option value=\"Costa Rica\">Costa Rica</option><option value=\"Croatia\">Croatia</option><option value=\"Cyprus\">Cyprus</option><option value=\"Czech Republic\">Czech Republic</option><option value=\"Denmark\">Denmark</option><option value=\"Dominican Republic\">Dominican Republic</option><option value=\"Ecuador\">Ecuador</option><option value=\"Egypt\">Egypt</option><option value=\"El Salvador\">El Salvador</option><option value=\"Estonia\">Estonia</option><option value=\"Finland\">Finland</option><option value=\"France\">France</option><option value=\"Germany\">Germany</option><option value=\"Greece\">Greece</option><option value=\"Guatemala\">Guatemala</option><option value=\"Haiti\">Haiti</option><option value=\"Honduras\">Honduras</option><option value=\"Hungary\">Hungary</option><option value=\"Iceland\">Iceland</option><option value=\"Ireland\">Ireland</option><option value=\"Israel\">Israel</option><option value=\"Italy\">Italy</option><option value=\"Jamaica\">Jamaica</option><option value=\"Japan\">Japan</option><option value=\"Jordan\">Jordan</option><option value=\"Kenya\">Kenya</option><option value=\"Kosovo\">Kosovo</option><option value=\"Latvia\">Latvia</option><option value=\"Lithuania\">Lithuania</option><option value=\"Luxembourg\">Luxembourg</option><option value=\"Mexico\">Mexico</option><option value=\"Moldova, Republic of\">Moldova, Republic of</option><option value=\"Mozambique\">Mozambique</option><option value=\"Netherlands\">Netherlands</option><option value=\"New Zealand\">New Zealand</option><option value=\"Nicaragua\">Nicaragua</option><option value=\"Norway\">Norway</option><option value=\"Pakistan\">Pakistan</option><option value=\"Panama\">Panama</option><option value=\"Paraguay\">Paraguay</option><option value=\"Peru\">Peru</option><option value=\"Poland\">Poland</option><option value=\"Portugal\">Portugal</option><option value=\"Puerto Rico\">Puerto Rico</option><option value=\"Romania\">Romania</option><option value=\"Russian Federation\">Russian Federation</option><option value=\"Saudi Arabia\">Saudi Arabia</option><option value=\"Serbia\">Serbia</option><option value=\"Singapore\">Singapore</option><option value=\"Slovakia\">Slovakia</option><option value=\"Slovenia\">Slovenia</option><option value=\"South Africa\">South Africa</option><option value=\"South Korea\">South Korea</option><option value=\"Spain\">Spain</option><option value=\"Sweden\">Sweden</option><option value=\"Switzerland\">Switzerland</option><option value=\"Trinidad And Tobago\">Trinidad And Tobago</option><option value=\"Tunisia\">Tunisia</option><option value=\"Turkey\">Turkey</option><option value=\"Ukraine\">Ukraine</option><option value=\"United Arab Emirates\">United Arab Emirates</option><option value=\"United Kingdom\">United Kingdom</option><option value=\"United States\">United States</option><option value=\"Uruguay\">Uruguay</option><option value=\"Venezuela\">Venezuela</option></select>";
    static final String HTML_PRODUCT = "<select class=\"form-control dark form-control form-select user-success\" id=\"dealerProduct\" name=\"product\"><option value=\"2339\">Agriculture</option><option value=\"2340\">Vertical Construction</option><optgroup label=\"Construction\"><option value=\"2341\">Construction</option><option value=\"2347\">Site and Grade Management</option><option value=\"2345\">Grading, Excavating &amp; Mass Haul</option><option value=\"2346\">Asphalt Paving</option><option value=\"5496\">3D Asphalt &amp; Concrete Paving</option><option value=\"5501\">Intelligent Paving</option><option value=\"5506\">Intelligent Compaction</option><option value=\"5511\">2D Milling &amp; Paving</option><option value=\"5336\">Levels and Optical Instruments</option><option value=\"2343\">Lasers</option><option value=\"5351\">Construction Software</option></optgroup><optgroup label=\"Geopositioning\"><option value=\"2349\">Geopositioning</option><option value=\"2354\">Field Surveying</option><option value=\"2350\">Design and Management</option><option value=\"2351\">Mobile Mapping</option><option value=\"2352\">Aerial Mapping</option><option value=\"2353\">Laser Scanning</option><option value=\"2355\">GNSS</option><option value=\"2721\">Layout</option><option value=\"5341\">Levels and Manual Optical</option></optgroup><option value=\"2356\">Mapping/GIS</option><option value=\"2359\">Forensics</option><option value=\"2360\">Deformation Monitoring</option><option value=\"2348\">Forestry</option><option value=\"5191\">Networks</option><option value=\"2357\">Mining</option><option value=\"5186\">OEM</option><option value=\"2361\">Education</option></select>";

    static final String OUT_DIR = "result/out";

    public static void main(String[] args) throws IOException, InterruptedException {
        Map<String, String> productMap = parseSelect(HTML_PRODUCT);
        Map<String, String> countryMap = parseSelect(HTML_COUNTRY);

        for (Map.Entry<String, String> productEntry : productMap.entrySet()) {
            String outputFilename = productEntry.getValue();
            outputFilename = outputFilename.replaceAll("[\\\\/]", " ");
            File outputFile = new File(OUT_DIR + File.separator + outputFilename + ".txt");
            if (outputFile.exists()) {
                //outputFile.delete();
                continue;
            }
            for (Map.Entry<String, String> countryEntry : countryMap.entrySet()) {
                String encodedCountry = URLEncoder.encode(countryEntry.getKey(), "UTF-8");
                System.out.println(String.format("getting form data from product: %s, country: %s", productEntry.getValue(), countryEntry.getValue()));
                String response = post(productEntry.getKey(), encodedCountry);
                String line = String.format("%s|%s|%s%s", productEntry.getValue(), countryEntry.getValue(), response.trim(), System.lineSeparator());
                FileUtils.write(outputFile, line, StandardCharsets.UTF_8, true);
                Thread.sleep(500);
            }
            Thread.sleep(1000);
        }

        List<TopPositioningDealer> resultDealers = new ArrayList<>();
        File outDir = new File(OUT_DIR);
        for (File productFile : outDir.listFiles()) {
            List<String> lines = FileUtils.readLines(productFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                int idx0 = line.indexOf("|");
                int idx1 = line.indexOf("|", idx0 + 1);
                String product = line.substring(0, idx0);
                String country = line.substring(idx0 + 1, idx1);
                String dealerInfo = line.substring(idx1 + 1);

                JSONArray jsonArr = JSON.parseArray(dealerInfo);
                for (Object obj : jsonArr) {
                    JSONObject jsonObject = (JSONObject) obj;
                    String command = jsonObject.getString("command");
                    if (command.equals("insert")) {
                        String data = jsonObject.getString("data");
                        Document doc = Jsoup.parse(data);
                        Elements dealerListEle = doc.select("div.dealer-list-carousel > div");
                        for (Element dealerEle : dealerListEle) {
                            Element mediaBodyEle = dealerEle.selectFirst("div.media-body");
                            if (mediaBodyEle == null) {
                                continue;
                            }

                            String head = null;
                            String url = null;
                            Element headEle = mediaBodyEle.selectFirst("h5.media-heading > a");
                            if (headEle == null) {
                                headEle = mediaBodyEle.selectFirst("h5.media-heading");
                            } else {
                                url = headEle.attr("href");
                            }
                            if (headEle != null) {
                                head = headEle.text();
                            }

                            String address = null;
                            String phone = null;
                            Element addressEle = mediaBodyEle.selectFirst("div.office-address");
                            if (addressEle != null) {
                                address = addressEle.text();
                            }
                            Element phoneEle = mediaBodyEle.selectFirst("div.office-phone");
                            if (phoneEle != null) {
                                phone = phoneEle.text();
                            }

                            TopPositioningDealer dealer = new TopPositioningDealer(product, country, head, url, address, phone);
                            resultDealers.add(dealer);
                        }
                    }
                }
            }
        }

        Workbook workbook = export(resultDealers);
        Util.write(workbook, "/home/justin/workspace/html-extractor/result/toppositioning-dealer-result.xlsx");
    }

    static Map<String, String> parseSelect(String selectHtml) {
        Document productDoc = Jsoup.parse(selectHtml);
        Elements optionEles = productDoc.select("option");
        Map<String, String> map = new HashMap<>();
        for (Element optionEle : optionEles) {
            String code = optionEle.attr("value");
            String name = optionEle.text();
            map.put(code, name);
        }
        return map;
    }

    static final OkHttpClient client = new OkHttpClient().newBuilder().build();
    static String post(String product, String country) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String keyPart = String.format("product=%s&latitude=22.5455&longitude=114.0683&country=%s", product, country);
        RequestBody body = RequestBody.create(mediaType, keyPart + "&postalCode=&submitted_dealer_id=&dealer=&form_build_id=form-DQFHE9Pjk96qN5e1gDAGLODkROADXT049HN7Iy6SNP0&form_id=topcon_services_find_a_dealer_form&_triggering_element_name=op&_triggering_element_value=Find+Dealers&ajax_html_ids%5B%5D=demandbase_js_lib&ajax_html_ids%5B%5D=marvel&ajax_html_ids%5B%5D=navbar&ajax_html_ids%5B%5D=not-published&ajax_html_ids%5B%5D=tcgn-app-bar&ajax_html_ids%5B%5D=product_launcher&ajax_html_ids%5B%5D=insights-link&ajax_html_ids%5B%5D=search&ajax_html_ids%5B%5D=insights-link&ajax_html_ids%5B%5D=search-container&ajax_html_ids%5B%5D=search-typeahead&ajax_html_ids%5B%5D=suggestions-wrapper&ajax_html_ids%5B%5D=typeahead-spinner&ajax_html_ids%5B%5D=suggestions-container&ajax_html_ids%5B%5D=close-search&ajax_html_ids%5B%5D=views-exposed-form-search-page-block-1&ajax_html_ids%5B%5D=edit-query-wrapper&ajax_html_ids%5B%5D=edit-query&ajax_html_ids%5B%5D=edit-submit-search-page&ajax_html_ids%5B%5D=block-menu-block-1&ajax_html_ids%5B%5D=product_infrastructure&ajax_html_ids%5B%5D=product_agriculture&ajax_html_ids%5B%5D=hero-wrapper&ajax_html_ids%5B%5D=hero-main&ajax_html_ids%5B%5D=hero-container&ajax_html_ids%5B%5D=close-hero-video&ajax_html_ids%5B%5D=hero-video&ajax_html_ids%5B%5D=primary-content-wrapper&ajax_html_ids%5B%5D=primary-content&ajax_html_ids%5B%5D=offers-carousel&ajax_html_ids%5B%5D=tabCollection&ajax_html_ids%5B%5D=tabCollectionContent&ajax_html_ids%5B%5D=tab0&ajax_html_ids%5B%5D=tab1&ajax_html_ids%5B%5D=tab2&ajax_html_ids%5B%5D=tab3&ajax_html_ids%5B%5D=tab4&ajax_html_ids%5B%5D=tab5&ajax_html_ids%5B%5D=tab6&ajax_html_ids%5B%5D=tab7&ajax_html_ids%5B%5D=tab8&ajax_html_ids%5B%5D=tab9&ajax_html_ids%5B%5D=tab10&ajax_html_ids%5B%5D=tab11&ajax_html_ids%5B%5D=insights-carousel&ajax_html_ids%5B%5D=dealerModal&ajax_html_ids%5B%5D=dealerModalLabel&ajax_html_ids%5B%5D=dealer-form-container&ajax_html_ids%5B%5D=topcon-services-find-a-dealer-form&ajax_html_ids%5B%5D=edit-form-container--7&ajax_html_ids%5B%5D=findDealerMsg&ajax_html_ids%5B%5D=edit-fields--6&ajax_html_ids%5B%5D=dealerProduct&ajax_html_ids%5B%5D=dealerCountry&ajax_html_ids%5B%5D=edit-submit-row--6&ajax_html_ids%5B%5D=dealerPostal&ajax_html_ids%5B%5D=dealerSubmit&ajax_html_ids%5B%5D=edit-dealer-list-container--6&ajax_html_ids%5B%5D=dealerModalRequestDemo&ajax_html_ids%5B%5D=dealerModalRequestDemoLabel&ajax_html_ids%5B%5D=topcon_demo_download-form-OZJ91MA7U7XsjJVeoyzFZzYAmhrsAFp6y9t8k3HjZwA&ajax_html_ids%5B%5D=topcon-demo-download-form-request-demo&ajax_html_ids%5B%5D=&ajax_html_ids%5B%5D=edit-fields-container&ajax_html_ids%5B%5D=edit-top-row&ajax_html_ids%5B%5D=edit-name-container&ajax_html_ids%5B%5D=edit-firstname&ajax_html_ids%5B%5D=edit-lastname&ajax_html_ids%5B%5D=edit-email-container&ajax_html_ids%5B%5D=edit-email&ajax_html_ids%5B%5D=edit-interest-container&ajax_html_ids%5B%5D=edit-interest-company-news&ajax_html_ids%5B%5D=edit-optional-postalcode&ajax_html_ids%5B%5D=edit-phone-container&ajax_html_ids%5B%5D=edit-mobile-phone-group&ajax_html_ids%5B%5D=dd-formCountryIndic&ajax_html_ids%5B%5D=edit-phone&ajax_html_ids%5B%5D=edit-sms&ajax_html_ids%5B%5D=edit-company-container&ajax_html_ids%5B%5D=edit-companyname&ajax_html_ids%5B%5D=edit-companyphone&ajax_html_ids%5B%5D=edit-address-container&ajax_html_ids%5B%5D=edit-streetaddress&ajax_html_ids%5B%5D=edit-city&ajax_html_ids%5B%5D=edit-address-2-container&ajax_html_ids%5B%5D=dd-formCountry&ajax_html_ids%5B%5D=dd-formState&ajax_html_ids%5B%5D=dd-formState-proxy&ajax_html_ids%5B%5D=edit-address-3-container&ajax_html_ids%5B%5D=edit-zip&ajax_html_ids%5B%5D=edit-country&ajax_html_ids%5B%5D=edit-state&ajax_html_ids%5B%5D=edit-description&ajax_html_ids%5B%5D=edit-industry-container&ajax_html_ids%5B%5D=edit-industry-earthworks&ajax_html_ids%5B%5D=edit-industry-survey-mapping&ajax_html_ids%5B%5D=edit-industry-agriculture&ajax_html_ids%5B%5D=edit-industry-paving&ajax_html_ids%5B%5D=edit-industry-building-construction&ajax_html_ids%5B%5D=edit-industry-oem&ajax_html_ids%5B%5D=edit-captcha-container&ajax_html_ids%5B%5D=edit-submit&ajax_html_ids%5B%5D=edit-location&ajax_html_ids%5B%5D=lglocModal&ajax_html_ids%5B%5D=lglocModalLabel&ajax_html_ids%5B%5D=block-menu-menu-footer-menu&ajax_html_ids%5B%5D=industry_dropdown&ajax_html_ids%5B%5D=product_dropdown&ajax_html_ids%5B%5D=product_infrastructure&ajax_html_ids%5B%5D=product_agriculture&ajax_html_ids%5B%5D=createAccountModal&ajax_html_ids%5B%5D=createAccountModalLabel&ajax_html_ids%5B%5D=topcon-services-signup-form-container&ajax_html_ids%5B%5D=topcon-services-signup-form&ajax_html_ids%5B%5D=createAccountMsg&ajax_html_ids%5B%5D=edit-form-container--2&ajax_html_ids%5B%5D=edit-names&ajax_html_ids%5B%5D=createAccountFirstName&ajax_html_ids%5B%5D=createAccountLastName&ajax_html_ids%5B%5D=createAccountEmail&ajax_html_ids%5B%5D=createAccountCompanyName&ajax_html_ids%5B%5D=createAccountPhoneNumber&ajax_html_ids%5B%5D=createAccountPassword&ajax_html_ids%5B%5D=createAccountPasswordConfirm&ajax_html_ids%5B%5D=createAccountSubmit&ajax_html_ids%5B%5D=gtm-jq-ajax-listen&ajax_html_ids%5B%5D=&ajax_html_ids%5B%5D=&ajax_html_ids%5B%5D=&ajax_html_ids%5B%5D=cboxOverlay&ajax_html_ids%5B%5D=colorbox&ajax_html_ids%5B%5D=cboxWrapper&ajax_html_ids%5B%5D=cboxTopLeft&ajax_html_ids%5B%5D=cboxTopCenter&ajax_html_ids%5B%5D=cboxTopRight&ajax_html_ids%5B%5D=cboxMiddleLeft&ajax_html_ids%5B%5D=cboxContent&ajax_html_ids%5B%5D=cboxTitle&ajax_html_ids%5B%5D=cboxCurrent&ajax_html_ids%5B%5D=cboxPrevious&ajax_html_ids%5B%5D=cboxNext&ajax_html_ids%5B%5D=cboxSlideshow&ajax_html_ids%5B%5D=cboxLoadingOverlay&ajax_html_ids%5B%5D=cboxLoadingGraphic&ajax_html_ids%5B%5D=cboxMiddleRight&ajax_html_ids%5B%5D=cboxBottomLeft&ajax_html_ids%5B%5D=cboxBottomCenter&ajax_html_ids%5B%5D=cboxBottomRight&ajax_html_ids%5B%5D=db_bw_pixel_ad&ajax_html_ids%5B%5D=db_lr_pixel_ad&ajax_html_ids%5B%5D=&ajax_html_ids%5B%5D=lo-cs-frame&ajax_html_ids%5B%5D=lo_chat_box&ajax_html_ids%5B%5D=lo_chat_top&ajax_html_ids%5B%5D=lo_chat_min_box&ajax_html_ids%5B%5D=lo_chat_end&ajax_html_ids%5B%5D=lo_chat_min&ajax_html_ids%5B%5D=lo_operator&ajax_html_ids%5B%5D=lo_chat_log&ajax_html_ids%5B%5D=lo_chat_input&ajax_html_ids%5B%5D=lo_chat_textarea&ajax_html_ids%5B%5D=lo_chat_sound_holder&ajax_html_ids%5B%5D=lo_chat_sound&ajax_html_ids%5B%5D=lo_chat_status&ajax_html_ids%5B%5D=lo_chat_submit_btn&ajax_html_ids%5B%5D=lo_poweredBy&ajax_page_state%5Btheme%5D=topconglobal&ajax_page_state%5Btheme_token%5D=lpVgiyzivEO_HlABLTE6eHRZWpaAvI48cseUCGMmUyY&ajax_page_state%5Bcss%5D%5Bmodules%2Fsystem%2Fsystem.base.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fdate%2Fdate_api%2Fdate.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fdate%2Fdate_popup%2Fthemes%2Fdatepicker.1.7.css%5D=1&ajax_page_state%5Bcss%5D%5Bmodules%2Ffield%2Ftheme%2Ffield.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Ffind_content%2Ffind_content.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fworkflow%2Fworkflow_admin_ui%2Fworkflow_admin_ui.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fviews%2Fcss%2Fviews.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Flingotek%2Fstyle%2Flingotek-base.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fckeditor%2Fcss%2Fckeditor.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fcolorbox%2Fstyles%2Fdefault%2Fcolorbox_style.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Fctools%2Fcss%2Fctools.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fmodules%2Frate%2Frate.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fdist%2Fstyle-blessed2.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fdist%2Fstyle-blessed1.css%5D=1&ajax_page_state%5Bcss%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fdist%2Fstyle.css%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Fbootstrap%2Fjs%2Fbootstrap.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fjquery_update%2Freplace%2Fjquery%2F1.7%2Fjquery.min.js%5D=1&ajax_page_state%5Bjs%5D%5Bmisc%2Fjquery-extend-3.4.0.js%5D=1&ajax_page_state%5Bjs%5D%5Bmisc%2Fjquery.once.js%5D=1&ajax_page_state%5Bjs%5D%5Bmisc%2Fdrupal.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fjquery_update%2Freplace%2Fui%2Fexternal%2Fjquery.cookie.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fjquery_update%2Freplace%2Fmisc%2Fjquery.form.min.js%5D=1&ajax_page_state%5Bjs%5D%5Bmisc%2Fajax.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fjquery_update%2Fjs%2Fjquery_update.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fmove_user%2Fmove-user.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Flibraries%2Fcolorbox%2Fjquery.colorbox-min.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fcolorbox%2Fjs%2Fcolorbox.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fcolorbox%2Fstyles%2Fdefault%2Fcolorbox_style.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fcolorbox%2Fjs%2Fcolorbox_load.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fviews%2Fjs%2Fbase.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Fbootstrap%2Fjs%2Fmisc%2F_progress.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fviews%2Fjs%2Fajax_view.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Fcaptcha%2Fcaptcha.js%5D=1&ajax_page_state%5Bjs%5D%5Bhttps%3A%2F%2Fwww.google.com%2Frecaptcha%2Fapi.js%3Fhl%3Den%26onload%3DdrupalRecaptchaOnload%26render%3Dexplicit%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Frecaptcha%2Fjs%2Frecaptcha.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Ftopcon_demo_download%2Fjs%2Ftopcon_demo_download.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fmodules%2Ftopcon_services%2Fjs%2Ftopcon_services.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Faffix.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Falert.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fbutton.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fcarousel.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fcollapse.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fdropdown.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fmodal.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Ftooltip.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fpopover.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Fscrollspy.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Ftab.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Ftopconglobal%2Fbootstrap%2Fjs%2Ftransition.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Fbootstrap%2Fjs%2Fmodules%2Fviews%2Fjs%2Fajax_view.js%5D=1&ajax_page_state%5Bjs%5D%5Bsites%2Fall%2Fthemes%2Fbootstrap%2Fjs%2Fmisc%2Fajax.js%5D=1&ajax_page_state%5Bjquery_version%5D=1.7");
        Request request = new Request.Builder()
                .url("https://www.topconpositioning.com/system/ajax")
                .method("POST", body)
                .addHeader("Connection", "keep-alive")
                .addHeader("Pragma", "no-cache")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36")
                .addHeader("sec-ch-ua-platform", "\"Linux\"")
                .addHeader("Origin", "https://www.topconpositioning.com")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Referer", "https://www.topconpositioning.com/")
                .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                .addHeader("Cookie", "tes_profile=false; SSESSa964ce96250a7727c5a40ad4f996dff8=S52C0pSeybSrb4rLvyNCTwus_6-jyXo2U7ZRkUGkwT0; has_js=1; _gcl_au=1.1.1299078022.1645669798; _ga=GA1.2.203477241.1645669804; _gid=GA1.2.1338562538.1645669804; tps_location_shown=1; _lo_uid=234733-1645669812534-9f4ae925cac2d85b; __lotl=https%3A%2F%2Fwww.topconpositioning.com%2F%23dealerModal; BE_CLA3=p_id%3DP6RRA2J44N64RPJA464RJ4468AAAAAAAAH%26bf%3D6bb6c0f94346902967666da8eea7b477%26bn%3D13%26bv%3D3.43%26s_expire%3D1645842036749%26s_id%3DP6RRA2J44N64R4APR2LRJ4468AAAAAAAAH; mp_702e37e1d3fc1c6b4c5a4e67e1b29a1a_mixpanel=%7B%22distinct_id%22%3A%20%2217f299478a692f-02696ba6cee617-1b2b1204-1e6000-17f299478a784a%22%2C%22%24device_id%22%3A%20%2217f299478a692f-02696ba6cee617-1b2b1204-1e6000-17f299478a784a%22%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%7D; _lorid=234733-1645757830538-aa949479f58684d7; _lo_v=11; SSESSa964ce96250a7727c5a40ad4f996dff8=xkhyQ_j4_hjyhTfODN9hnE7PRMsOZMtR3aozknTI7tc")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response == null ? "" : response.body().string();
        } catch (IOException e) {
            System.err.println("error request for product: " + product + ", country: " + country);
            throw e;
        }
    }

    static List<Util.Header> CELL_HEADERS = new ArrayList<>();
    static {
        CELL_HEADERS.add(new Util.Header("Product", 6000));
        CELL_HEADERS.add(new Util.Header("Country", 3000));
        CELL_HEADERS.add(new Util.Header("Head", 8000));
        CELL_HEADERS.add(new Util.Header("URL", 8000));
        CELL_HEADERS.add(new Util.Header("Address", 12000));
        CELL_HEADERS.add(new Util.Header("Phone", 4000));
    }

    static Workbook export(List<TopPositioningDealer> dealers) {
        Workbook workbook = new SXSSFWorkbook();
        // sheet header
        Sheet sheet = Util.buildSheet(workbook, CELL_HEADERS);

        //构建每行的数据内容
        int rowNum = 1;
        for (TopPositioningDealer dealer : dealers) {
            if (dealer == null) {
                continue;
            }
            //输出行数据
            Row row = sheet.createRow(rowNum++);
            convertDataToRow(dealer, row);
        }
        return workbook;
    }

    static void convertDataToRow(TopPositioningDealer dealer, Row row) {
        int cellNumber = 0;
        Util.createCell(cellNumber++, row, dealer.getProduct());
        Util.createCell(cellNumber++, row, dealer.getCountry());
        Util.createCell(cellNumber++, row, dealer.getHead());
        Util.createCell(cellNumber++, row, dealer.getUrl());
        Util.createCell(cellNumber++, row, dealer.getAddress());
        Util.createCell(cellNumber++, row, dealer.getPhone());
    }
}
