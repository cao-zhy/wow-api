package com.github.czy211.wowapi.ui.pane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WikiPane extends BasePane {
    public static final String BASE_URI = "https://wowpedia.fandom.com";
    private final HashMap<String, String> docs = new HashMap<>();
    
    public WikiPane(String name, String extension, String url) {
        super(name, extension, url);
        docs.put("Region:CreateAnimationGroup", "---@return AnimationGroup\n");
        docs.put("Frame:CreateFontString", "---@return FontString\n");
        docs.put("Frame:CreateTexture", "---@return Texture\n");
    }
    
    public void resolve(String prefix, String query, String endFlag, HashMap<String, String[]> functionsToCopy) {
        StringBuilder result = new StringBuilder(prefix);
        ArrayList<String> list = new ArrayList<>();
        Elements elements = connect(query);
        for (Element element : elements) {
            if (Thread.currentThread().isInterrupted()) {
                canceled();
                return;
            }
            if (endFlag != null && endFlag.equals(element.tagName())) {
                break;
            }
            StringBuilder argsToEnd = addFunctions(result, element, list);
            if (functionsToCopy != null) {
                String fullName = element.selectFirst("a[title^=API ]").text();
                String oldWidgetName = fullName.split(":")[0];
                String[] widgetNames = functionsToCopy.get(oldWidgetName);
                if (widgetNames != null) {
                    for (String newName : widgetNames) {
                        result.append("function ").append(fullName.replaceFirst(oldWidgetName, newName))
                                .append(argsToEnd);
                    }
                }
            }
            increaseCurrent(1);
        }
        createFile(result);
    }
    
    public Elements connect(String query) {
        String url = getUrl();
        try {
            Elements elements = Jsoup.connect(url).get().select(query);
            if (elements != null) {
                connected(elements.size());
                return elements;
            }
        } catch (IOException e) {
            connectFail(url);
            e.printStackTrace();
        }
        return null;
    }
    
    public String getPageDateTime() throws IOException {
        Element element = Jsoup.connect(getUrl()).get().selectFirst("#footer-info-lastmod");
        String dateTime = element.text().substring(29);
        return formatDateTime(dateTime, "d MMMM yyyy, 'at' HH:mm.");
    }
    
    public String formatDateTime(String dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return parseFormatter.format(LocalDateTime.parse(dateTime, formatter));
    }
    
    public String getLink(String prefix, String suffix, Element element, String query) {
        Element link = element.selectFirst(query);
        String title = link.attr("title");
        String href = link.attr("href");
        if (!"".equals(title) && !"".equals(href) && !title.endsWith("(page does not exist)")) {
            return prefix + BASE_URI + href + suffix;
        }
        return "";
    }
    
    private StringBuilder addFunctions(StringBuilder sb, Element element, ArrayList<String> list) {
        String linkQuery = "a[title^=API ]";
        Element el = element.selectFirst(linkQuery);
        String name = el.text();
        
        String namespace = getNamespace(name);
        if (namespace != null && !list.contains(namespace)) {
            list.add(namespace);
            sb.append(namespace).append(" = {}\n\n");
        }
        
        String text = element.text();
        String description = getDescription(text);
        
        String link = getLink("\n---\n--- [", "]", element, linkQuery);
        sb.append("--- ").append(description).append(link).append("\n");
        
        String doc = docs.get(name);
        if (doc != null) {
            sb.append(doc);
        }
        
        sb.append("function ").append(name);
        StringBuilder argsToEnd = new StringBuilder("(" + getArgs(text) + ") end\n\n");
        sb.append(argsToEnd);
        return argsToEnd;
    }
    
    private String getArgs(String s) {
        Matcher m = Pattern.compile(".*\\((.*)\\).*").matcher(s);
        if (m.find()) {
            return m.group(1).isEmpty() ? "" : "...";
        }
        return "";
    }
    
    private String getDescription(String s) {
        return s.replaceAll("\\[", "{").replaceAll("]", "}");
    }
    
    private String getNamespace(String s) {
        int index = s.indexOf(".");
        return index == -1 ? null : s.substring(0, index);
    }
}
