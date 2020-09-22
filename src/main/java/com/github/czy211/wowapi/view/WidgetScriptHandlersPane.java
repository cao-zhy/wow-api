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

public class WidgetScriptHandlersPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_script_handlers";

    public WidgetScriptHandlersPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            Document document = Jsoup.connect(API_URL).get();
            connectSuccess();

            Elements elements = document.select("span.mw-headline:not(#Widget_API,#Widget_hierarchy,#Example,"
                    + "#References),dd:has(a[title^=UIHANDLER ]:eq(0))");
            int total = elements.size();
            int current = 0;
            for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                if ("span".equals(element.tagName())) {
                    if (current > 1) {
                        sb.append("    },\n");
                    }
                    sb.append("    ").append(text).append(" = {\n");
                } else {
                    sb.append("        \"").append(text).append("\",\n");
                }
            }
            if (sb.length() > 0) {
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                    writer.println(EnumVersionType.PREFIX + getRemoteVersion());
                    writer.println("\nlocal widgetScriptHandlers = {");
                    writer.print(sb);
                    writer.println("    },\n}");
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
