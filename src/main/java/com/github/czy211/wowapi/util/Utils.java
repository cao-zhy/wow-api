package com.github.czy211.wowapi.util;

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
}
