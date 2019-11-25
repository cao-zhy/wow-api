package com.github.czy211.wowapi.model;

import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Page {
    private static final String BASE_URL = "https://wow.gamepedia.com";
    private final String type;
    private final String path;
    private final String stopFuncName;
    private final String fileName;
    private String outputPath;

    public Page(String type, String path, String fileName) {
        this(type, path, fileName, "");
    }

    public Page(String type, String path, String fileName, String stopFuncName) {
        this.type = type;
        this.path = path;
        this.fileName = fileName;
        this.stopFuncName = stopFuncName;
    }

    /**
     * 爬取网页数据
     */
    public void crawl() {
        try {
            StringBuilder content = new StringBuilder();
            Document document = Jsoup.connect(BASE_URL + path).get();
            if ("function".equals(type)) {
                // 获取所有dd元素
                Elements elements = document.getElementsByTag("dd");
                for (Element element : elements) {
                    // 获取dd元素的第一个超链接
                    Element link = element.selectFirst("a");
                    if (link == null) {
                        continue;
                    }
                    // 获取超链接href属性的字符串内容
                    String linkHref = link.attr("href");
                    if (!Utils.isAPIElement(linkHref)) {
                        continue;
                    }
                    // 获取dd元素的文本内容并将[]替换为{}，因为[]在注释中会被删除
                    String description = element.text().replaceAll("\\[", "{").replaceAll("]", "}");
                    if (Utils.isUIFunc(description) || Utils.isRemovedFunc(description)) {
                        continue;
                    }
                    String url = Utils.pageNotExist(linkHref) ? "" : (BASE_URL + linkHref);
                    // 获取超链接的文本内容，即函数名
                    String name = link.text();
                    // 停止爬取
                    if (stopFuncName.equals(name)) {
                        break;
                    }
                    String s = description.split(" - ", 2)[0];
                    int startPos = s.indexOf(name) + name.length();
                    // 防止少一个右括号的情况
                    int endPos = s.length() - (s.endsWith(")") ? 1 : 0);
                    // 描述中括号里的内容
                    String argString = s.substring(startPos, endPos);
                    // 处理括号里的字符串，使之符合参数值的要求，还有不合法的参数会在Function类的toString方法中做进一步处理
                    String[] args = argString.replaceAll(" or |/|\\||\\\\", "_")
                            .replaceAll("\\{|}|\"|\\(|\\)|-|\\w+\\.| +|\\+", "").replaceAll("\\w+\\.{3}", "...")
                            .split(",");
                    ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
                    // 创建函数对象并添加到结果中
                    Function function = new Function(description, url, name, arguments);
                    content.append(function).append("\n");
                }
            }
            /* 获取当前文件路径，在IDE运行时返回class文件所在目录，即.../target/classes；在jar包中运行时返回jar包路径，
               即.../xxx.jar */
            String outputPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            // 处理在jar包运行的情况
            if (outputPath.endsWith(".jar")) {
                outputPath = outputPath.substring(0, outputPath.lastIndexOf("/") + 1);
            }
            this.outputPath = outputPath;
            // 将数据写入文件中
            Utils.writeData(outputPath, fileName, content.toString());
        } catch (IOException ex) {
            System.err.println("无法连接到 " + BASE_URL + path);
        }
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getFileName() {
        return fileName;
    }
}
