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
        String path = getConfigProperty(PropConst.DOWNLOAD_PATH);
        return path == null ? PathConst.DEFAULT_DOWNLOAD : path;
    }

    public static String getBicPath() {
        return getConfigProperty(PropConst.BIC_PATH);
    }

    public static String getConfigProperty(String property) {
        File configFilePath = new File(PathConst.CONFIG_FILE);
        if (configFilePath.exists() && configFilePath.isFile()) {
            Properties properties = new Properties();
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                return properties.getProperty(property);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void setConfigProperty(String property, String value) {
        File configFilePath = new File(PathConst.CONFIG_FILE);
        if (configFilePath.exists()) {
            Properties properties = new Properties();
            try {
                // 不加载 properties 就写入的话会清空之前的属性
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                try (PrintWriter writer = new PrintWriter(configFilePath, "UTF-8")) {
                    properties.setProperty(property, value);
                    properties.store(writer, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 创建配置文件并写入属性
            try (PrintWriter writer = new PrintWriter(configFilePath, "UTF-8")) {
                writer.println(property + "=" + value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
