package com.github.czy211.wowapi.util;

import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.constant.PathConst;
import com.github.czy211.wowapi.constant.PropConst;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

public class Utils {
    public static String getDownloadPath() {
        File configFilePath = new File(PathConst.CONFIG_FILE);
        if (configFilePath.exists() && configFilePath.isFile()) {
            Properties properties = new Properties();
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                String path = properties.getProperty(PropConst.DOWNLOAD_PATH);
                return path == null ? PathConst.DEFAULT_DOWNLOAD : path;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return PathConst.DEFAULT_DOWNLOAD;
    }

    public static long getRemoteTimestamp(String url) throws IOException {
        try {
            Document document = Jsoup.connect(url).get();
            Element element = document.selectFirst("#footer-info-lastmod");
            String dateTime = element.text().substring(29);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, 'at' HH:mm.", Locale.ENGLISH);
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            return zonedDateTime.toEpochSecond();
        } catch (IOException e) {
            throw new IOException(url, e);
        }
    }

    public static long getRemoteBuild(String filepath) throws IOException {
        String url = LinkConst.FXML_BASE + "/live";
        try {
            Document document = Jsoup.connect(url).get();
            String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
            Element tr = document.selectFirst("tr:contains(" + filename + ")");
            // 第二个 td 是 build 信息
            Element td = tr.select("td").get(1);
            String text = td.text();
            return Long.parseLong(text.substring(text.length() - 5));
        } catch (IOException e) {
            throw new IOException(url, e);
        }
    }

    /**
     * @return 第一个元素是文件的 build，第二个元素是游戏的 build
     */
    public static long[] getBuild() throws IOException {
        String url = LinkConst.FXML_BASE + "/live";
        try {
            Document document = Jsoup.connect(url).get();
            Element element = document.selectFirst("h1");
            long fileBuild = Long.parseLong(element.text().substring(6, 11));
            long gameBuild = fileBuild;
            Element moreBuilds = element.selectFirst(".morebuilds");
            if (moreBuilds != null) {
                String title = moreBuilds.attr("title");
                gameBuild = Long.parseLong(title.substring(1));
            }
            return new long[]{fileBuild, gameBuild};
        } catch (IOException e) {
            throw new IOException(url, e);
        }
    }
}
