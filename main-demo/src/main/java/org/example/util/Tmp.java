package org.example.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.example.entity.TrimbleDealer;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * May delete anyway: just for playing
 */
public class Tmp {
    public static void main(String[] args) throws IOException {
        String s = URLEncoder.encode("Beijing MAG Tianhong Science \\u0026 Technology Development Co., Ltd",
                StandardCharsets.UTF_8);

        String html = Jsoup.connect("https://code.metalocator.com/index.php?user_lat=0&user_lng=0&postal_code=&radius=200&country=CN&ml_location_override=&Itemid=15074&view=directory&layout=combined_bootstrap&tmpl=component&framed=1&ml_skip_interstitial=0&preview=0&parent_table=&parent_id=0&task=search_zip&search_type=point&_opt_out=0&option=com_locator&limitstart=0&filter_order=id&filter_order_Dir=asc")
                .get().outerHtml();

        final String PATTERN = "var location_data =";

        String jsonStr = null;
        for (String line : IOUtils.readLines(new StringReader(html))) {
            line = line.trim();
            if (line.startsWith(PATTERN)) {
                // remove semicolon ;
                jsonStr = line.substring(PATTERN.length(), line.length() - 1);
            }
        }
        JSONArray jsonArray = JSON.parseArray(jsonStr);
        if (jsonArray.size() == 0) {
            return;
        }
        for (Object obj : jsonArray) {
            JSONObject jo = (JSONObject) obj;
            TrimbleDealer dealer = new TrimbleDealer();
            dealer.setName(jo.getString("name"));
            dealer.setAddress(String.format("%s %s %s",
                    jo.getString("address"),
                    jo.getString("address2"),
                    jo.getString("city")));
            dealer.setCountry(jo.getString("country"));
            dealer.setLink(jo.getString("link"));
            dealer.setEmail(jo.getString("email"));
            dealer.setPhone(jo.getString("phone"));
            dealer.setPriorityName(jo.getString("priority_name"));

            System.out.println(dealer);
        }
    }
}
