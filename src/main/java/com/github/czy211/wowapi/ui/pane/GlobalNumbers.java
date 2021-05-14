package com.github.czy211.wowapi.ui.pane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalNumbers extends BasePane {
    private static final String name = "Global_Numbers";
    private static final String extension = ".lua";
    private static final String url = "https://github.com/czy211/wow-api/releases";
    private static final GlobalNumbers pane = new GlobalNumbers();
    private final String fileRegex;
    
    private GlobalNumbers() {
        super(name, extension, url);
        fileRegex = getFileRegex();
    }
    
    public static GlobalNumbers getInstance() {
        return pane;
    }
    
    @Override
    public void download() {
        String downloadUrl = "";
        try {
            Element element = Jsoup.connect(url).get().selectFirst("a:has(span:matches(" + fileRegex + "))");
            downloadUrl = "https://github.com" + element.attr("href");
        } catch (IOException e) {
            connectFail(url);
            e.printStackTrace();
        }
        downloadFile(downloadUrl);
    }
    
    @Override
    public String getRemoteVersion() throws IOException {
        Element element = Jsoup.connect(url).get().selectFirst("span:matches(" + fileRegex + ")");
        Matcher m = Pattern.compile(fileRegex).matcher(element.text());
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
