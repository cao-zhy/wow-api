package com.github.czy211.wowapi.model;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WikiPage {
    private String path;
    private String stopFuncName;
    private String name;
    private String fileName;
    private long timestamp;

    public WikiPage(String path, String name, String fileName) {
        this(path, name, fileName, "");
    }

    public WikiPage(String path, String name, String fileName, String stopFuncName) {
        this.path = path;
        this.name = name;
        this.fileName = fileName;
        this.stopFuncName = stopFuncName;
    }

    /**
     * 爬取网页数据
     */
    public String crawl() throws IOException {
        StringBuilder content = new StringBuilder();
        Document document = Jsoup.connect(Constants.WIKI_BASE_URL + path).get();
        timestamp = Utils.getTimestamp(document);
        content.append(Constants.COMMENT_TIMESTAMP).append(timestamp).append("\n\n");
        if ("WidgetAPI.lua".equals(fileName)) {
            content.append(Utils.getWidgetTypes()).append("\n\n");
        }
        if ("WidgetHandler.lua".equals(fileName)) {
            content.append("local widgetHandlers = {\n");

            ArrayList<Widget> widgets = new ArrayList<>();
            Elements elements = document.select("h2:has(span.mw-headline), h3:not(:contains(Inherits)), dd");
            for (Element element : elements) {
                String text = element.text();
                // 如果文本内容是 References，则已完成遍历
                if ("References".equals(text)) {
                    break;
                }
                // text 以 “On”、“Pre” 或 “Post” 开头时是 handler 方法
                if (text.startsWith("On") || text.startsWith("Pre") || text.startsWith("Post")) {
                    Widget widget = widgets.get(widgets.size() - 1);
                    widget.getHandlers().add(text);
                } else { // text 是 widget 名
                    Widget widget = new Widget(text);
                    widgets.add(widget);
                }
            }

            for (Widget widget : widgets) {
                content.append("    ").append(widget.getName()).append(" = {\n");
                for (String handler : widget.getHandlers()) {
                    content.append("        \"").append(handler).append("\",\n");
                }
                content.append("    },\n");
            }

            content.append("}\n");
        } else {
            Set<String> namespaces = new HashSet<>();
            // 获取所有 dd 元素
            Elements elements = document.getElementsByTag("dd");
            for (Element element : elements) {
                // 获取 dd 元素的第一个超链接
                Element link = element.selectFirst("a");
                if (link == null) {
                    continue;
                }
                // 获取超链接 href 属性的字符串内容
                String linkHref = link.attr("href");
                if (!Utils.isAPIElement(linkHref)) {
                    continue;
                }
                // 获取dd元素的文本内容并将 [] 替换为 {}，因为 [] 在注释中会被删除
                String description = element.text().replaceAll("\\[", "{").replaceAll("]", "}");
                if (Utils.skipFunc(description)) {
                    continue;
                }
                String url = Utils.pageNotExist(linkHref) ? "" : (Constants.WIKI_BASE_URL + linkHref);
                // 获取超链接的文本内容，即函数名
                String name = link.text();
                if (name.startsWith("C_")) {
                    namespaces.add(name.substring(0, name.indexOf(".")));
                }
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
                // 处理括号里的字符串，使之符合参数值的要求，还有不合法的参数会在 Function 类的 toString 方法中做进一步处理
                String[] args = argString.replaceAll(" or |/|\\||\\\\", "_")
                        .replaceAll("\\{|}|\"|\\(|\\)|-|\\w+\\.| +|\\+", "").replaceAll("\\w+\\.{3}", "...")
                        .split(",");
                ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
                // 创建函数对象并添加到结果中
                Function function = new Function(description, url, name, arguments);
                content.append(function).append("\n");
            }
            for (String namespace : namespaces) {
                content.append(namespace).append(" = {}\n");
            }
            if ("WidgetAPI.lua".equals(fileName)) {
                content.append(Utils.getParentFunctions(content.toString())).append("\n");
            }
        }
        return content.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }
}