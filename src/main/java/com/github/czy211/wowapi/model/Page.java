package com.github.czy211.wowapi.model;

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

public class Page {
    private static final String BASE_URL = "https://wow.gamepedia.com";
    private final String type;
    private final String path;
    private final String stopFuncName;
    private final String fileName;
    private String outputPath;
    private long timestamp;

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
    public String crawl() {
        try {
            StringBuilder content = new StringBuilder();
            Document document = Jsoup.connect(BASE_URL + path).get();

            String lastRevision = document.getElementById("footer-info-lastmod").text().substring(29);
            timestamp = Utils.getTimestamp(lastRevision);

            if ("function".equals(type)) {
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
                    // 处理括号里的字符串，使之符合参数值的要求，还有不合法的参数会在 Function 类的 toString 方法中做进一步处理
                    String[] args = argString.replaceAll(" or |/|\\||\\\\", "_")
                            .replaceAll("\\{|}|\"|\\(|\\)|-|\\w+\\.| +|\\+", "").replaceAll("\\w+\\.{3}", "...")
                            .split(",");
                    ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
                    // 创建函数对象并添加到结果中
                    Function function = new Function(description, url, name, arguments);
                    content.append(function).append("\n");
                }
            } else if ("handler".equals(type)) {
                ArrayList<Widget> widgets = new ArrayList<>();
                Set<String> allScriptTypes = new HashSet<>();
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
                        // 获取 script type 名
                        int end = text.length();
                        if (text.contains("(")) {
                            end = text.indexOf("(");
                        } else if (text.contains("-")) {
                            end = text.indexOf("-") - 1;
                        }
                        String scriptType = text.substring(0, end);
                        widget.getScriptTypes().add(scriptType);
                        // 添加到所有的脚本类型中
                        allScriptTypes.add(scriptType);
                    } else { // text 是 widget 名
                        Widget widget = new Widget(text);
                        widgets.add(widget);
                    }
                }

                content.append("---@alias ScriptType string ");
                for (String st : allScriptTypes) {
                    content.append("|'\"").append(st).append("\"'");
                }
                content.append("\n\n");
                for (Widget widget : widgets) {
                    String name = widget.getName();

                    content.append("---@alias ").append(name).append("ScriptType string ");
                    for (String st : widget.getScriptTypes()) {
                        content.append("|'\"").append(st).append("\"'");
                    }
                    // AnimationGroup、Animation、Frame 需要添加 ScriptObject 中的 ScriptType，因为它们没有继承 ScriptObject
                    if ("AnimationGroup".equals(name) || "Animation".equals(name) || "Frame".equals(name)) {
                        for (String st : widgets.get(0).getScriptTypes()) {
                            content.append("|'\"").append(st).append("\"'");
                        }
                    }
                    content.append("\n\n");

                    content.append("---@param scriptType ScriptType\n");
                    content.append("function ").append(name).append(":GetScript(scriptType) end\n\n");
                    content.append("---@param scriptType ScriptType\n");
                    content.append("function ").append(name).append(":HasScript(scriptType) end\n\n");
                    content.append("---@param scriptType ").append(name).append("ScriptType\n");
                    content.append("---@param handler function\n");
                    content.append("function ").append(name).append(":HookScript(scriptType, handler) end\n\n");
                    content.append("---@param scriptType ").append(name).append("ScriptType\n");
                    content.append("---@param handler function\n");
                    content.append("function ").append(name).append(":SetScript(scriptType, handler) end\n\n");
                }
            }

            /* 获取当前文件路径，在IDE运行时返回class文件所在目录，即 .../target/classes；在 jar 包中运行时返回 jar 包路径，
               即 .../xxx.jar */
            String outputPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            // 处理在 jar 包运行的情况
            if (outputPath.endsWith(".jar")) {
                outputPath = outputPath.substring(0, outputPath.lastIndexOf("/") + 1);
            }
            this.outputPath = outputPath;
            // 将数据写入文件中
            Utils.writeData(outputPath, fileName, content.toString());
        } catch (IOException ex) {
            System.err.println("无法连接到 " + BASE_URL + path);
        }
        return outputPath + "/" + fileName;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
