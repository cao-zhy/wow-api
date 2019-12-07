package com.github.czy211.wowapi.util;

import org.jsoup.nodes.Document;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

public class Utils {
    /**
     * 判断是否为 API 元素
     *
     * @param linkHref 链接的字符串内容
     * @return 如果是 API 元素则返回 true，否则返回 false
     */
    public static boolean isAPIElement(String linkHref) {
        return linkHref.startsWith("/API_") || pageNotExist(linkHref);
    }

    /**
     * 判断是否跳过该函数
     *
     * @param description 函数描述
     * @return 如果是 UI 函数或 REMOVED 函数或 DEPRECATED 函数则返回 true，否则返回 false
     */
    public static boolean skipFunc(String description) {
        return description.startsWith("UI ") || description.startsWith("REMOVED ")
                || description.startsWith("DEPRECATED ");
    }


    /**
     * 判断函数页面是否不存在
     *
     * @param linkHref 链接字符串内容
     * @return 如果函数页面不存在则返回 true，否则返回 false
     */
    public static boolean pageNotExist(String linkHref) {
        return linkHref.startsWith("/index.php");
    }

    /**
     * 获取部件类型的字符串
     *
     * @return 部件类型的字符串
     */
    public static String getWidgetTypes() {
        StringBuilder result = new StringBuilder();
        InputStream in = Utils.class.getResourceAsStream("/WidgetTypes.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 因为 EmmyLua 的 @class 注解目前只支持一个父类，所以 FontInstance 子类需要显式定义 FontInstance 类中的函数, ScriptObject
     * 子类需要显式定义 ScriptObject 类中的函数，@class 支持多重继承后可删除
     *
     * @param functions 函数字符串
     * @return 子类继承函数字符串
     */
    public static String getParentFunctions(String functions) {
        StringBuilder result = new StringBuilder();
        Scanner input = new Scanner(functions);
        while (input.hasNext()) {
            String line = input.nextLine();
            if (line.startsWith("function FontInstance:")) {
                result.append(line.replaceFirst("FontInstance", "FontString")).append("\n\n");
                result.append(line.replaceFirst("FontInstance", "EditBox")).append("\n\n");
                result.append(line.replaceFirst("FontInstance", "MessageFrame")).append("\n\n");
                result.append(line.replaceFirst("FontInstance", "ScrollingMessageFrame")).append("\n\n");
                result.append(line.replaceFirst("FontInstance", "SimpleHTML")).append("\n\n");
            } else if (line.startsWith("function ScriptObject:")) {
                result.append(line.replaceFirst("ScriptObject", "Animation")).append("\n\n");
                result.append(line.replaceFirst("ScriptObject", "AnimationGroup")).append("\n\n");
                result.append(line.replaceFirst("ScriptObject", "Frame")).append("\n\n");
            }
        }
        return result.toString();
    }

    public static long getTimestamp(Document document) {
        String lastRevision = document.getElementById("footer-info-lastmod").text().substring(29);
        return Utils.convertStringToTimestamp(lastRevision);
    }

    public static long convertStringToTimestamp(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, 'at' HH:mm.", Locale.ENGLISH);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }

    public static String convertTimestampToString(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.of("+8"));
        return formatter.format(localDateTime);
    }

    public static int getBuild(Document document) {
        String build = document.getElementsByTag("title").text().substring(17, 22);
        return Integer.parseInt(build);
    }

    public static String getOutputDirectory() {
        String path = getPath();
        File file = new File(path + "config.properties");
        if (file.exists()) {
            Properties properties = new Properties();
            try {
                InputStream inputStream = new FileInputStream(file);
                properties.load(inputStream);
                return properties.getProperty("outputPath");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    public static String getPath() {
        String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.endsWith(".jar")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
        }
        return path;
    }
}
