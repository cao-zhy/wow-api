package com.github.czy211.wowapi.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
     * 将内容写入到文件中
     *
     * @param fileName 待写入的文件
     * @param content 写入的内容
     */
    public static void writeData(String path, String fileName, String content) {
        File file = new File(path);
        if (file.exists() || file.mkdir()) {
            try (PrintWriter output = new PrintWriter(file.getPath() + "/" + fileName)) {
                output.print(content);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 将内容添加到文件的顶部 <br>
     * 函数类型最好应该生成一个新文件，但因为 EmmyLua 的问题，部件类型和方法不在同一文件时，点击子类调用父类的方法可能会无法正确跳转
     *
     * @param fileName 待写入的文件
     * @param content 写入的内容
     */
    public static void addHeader(String fileName, String content) {
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            int length = (int) raf.length();
            byte[] buff = new byte[length];
            raf.read(buff, 0, length);
            raf.seek(0);
            raf.write(content.getBytes());
            raf.seek(content.length());
            raf.write(buff);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
     * 因为 EmmyLua 的 @class 注解目前只支持一个父类，所以 FontInstance 子类需要显式定义 FontInstance 类中的方法，@class 支持多
     * 重继承后可删除。ScriptObject 子类继承 ScriptObject 的方法移至 WidgetHandler 处理
     *
     * @param file WidgetAPI.lua 文件
     * @return FontInstance 子类继承的方法
     */
    public static String replaceFontInstance(String file) {
        StringBuilder result = new StringBuilder();
        try (Scanner input = new Scanner(new File(file))) {
            while (input.hasNext()) {
                String line = input.nextLine();
                if (line.startsWith("function FontInstance:")) {
                    result.append(line.replaceFirst("FontInstance", "FontString")).append("\n\n");
                    result.append(line.replaceFirst("FontInstance", "EditBox")).append("\n\n");
                    result.append(line.replaceFirst("FontInstance", "MessageFrame")).append("\n\n");
                    result.append(line.replaceFirst("FontInstance", "ScrollingMessageFrame")).append("\n\n");
                    result.append(line.replaceFirst("FontInstance", "SimpleHTML")).append("\n\n");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    public static long getTimestamp(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, 'at' HH:mm.", Locale.ENGLISH);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }
}
