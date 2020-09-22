package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class WowApiPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/World_of_Warcraft_API";

    public WowApiPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            HashSet<String> namespaces = new HashSet<>();
            Document document = Jsoup.connect(API_URL).get();
            connectSuccess();

            Elements elements = document.select("dd:not(:matches(^(UI|DEPRECATED|REMOVED) )):has(a[title^=API ]:eq(0)),"
                    + "span#Classic_Specific_Functions");
            double total = elements.size();
            int current = 0;
            for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                current++;
                updateProgress(current / total);

                if ("span".equals(element.tagName())) {
                    // 遍历完成
                    break;
                }

                appendFunction(sb, element);

                String name = element.selectFirst("a").text();
                if (name.startsWith("C_")) {
                    String namespace = name.substring(0, name.indexOf("."));
                    namespaces.add(namespace);
                }
            }
            if (sb.length() > 0) {
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                    writer.println(EnumVersionType.PREFIX + getRemoteVersion());
                    writer.println();
                    for (String namespace : namespaces) {
                        writer.println(namespace + " = {}");
                    }
                    writer.println();
                    writer.println(sb);
                }
            }
        } catch (IOException e) {
            throw new IOException(API_URL, e);
        }
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return Utils.getRemoteTimestamp(API_URL);
    }
}
