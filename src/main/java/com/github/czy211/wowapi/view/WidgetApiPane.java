package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.constant.WidgetConst;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class WidgetApiPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_API";
    private static final HashMap<String, String[]> COPY_FUNCTIONS = new HashMap<>();

    static {
        COPY_FUNCTIONS.put("FontInstance", new String[]{"FontString", "EditBox", "MessageFrame", "SimpleHTML"});
        COPY_FUNCTIONS.put("Scale", new String[]{"LineScale"});
        COPY_FUNCTIONS.put("Translation", new String[]{"LineTranslation"});
        COPY_FUNCTIONS.put("Texture", new String[]{"MaskTexture", "Line"});
    }

    public WidgetApiPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            Document document = Jsoup.connect(API_URL).get();
            connectSuccess();

            Elements elements = document.select("dd:not(:matches(^(UI|DEPRECATED|REMOVED) ))"
                    + ":has(a[title^=API ]:eq(0))");
            int total = elements.size();
            int current = 0;

            for (Element element : elements) {
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                Element link = element.selectFirst("a");
                String title = link.attr("title");
                String description = text.replaceAll("\\[", "{").replaceAll("]", "}");
                String url = "";
                if (!title.endsWith("(page does not exist)")) {
                    url = "\n---\n--- [" + LinkConst.WIKI_BASE + link.attr("href") + "]";
                }
                String front = "--- " + description + url + "\nfunction ";
                String back = "(" + (text.indexOf(")") - text.indexOf("(") > 1 ? "..." : "") + ") end\n\n";
                String name = link.text();
                sb.append(front).append(name).append(back);

                // 复制函数
                String widgetName = name.split(":")[0];
                String[] widgets = COPY_FUNCTIONS.get(widgetName);
                if (widgets != null) {
                    for (String widget : widgets) {
                        sb.append("function ").append(name.replaceFirst(widgetName, widget)).append(back);
                    }
                }
            }
            if (sb.length() > 0) {
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                    writer.println(EnumVersionType.PREFIX + getRemoteVersion());
                    writer.println();
                    writer.print(WidgetConst.WIDGET_HIERARCHY);
                    writer.print(sb);
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
