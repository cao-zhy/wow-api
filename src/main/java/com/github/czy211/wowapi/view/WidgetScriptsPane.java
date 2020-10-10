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

public class WidgetScriptsPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_script_handlers";

    public WidgetScriptsPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(EnumVersionType.PREFIX).append(getRemoteVersion()).append("\n\n");
            Document document = Jsoup.connect(API_URL).get();
            Elements elements = document.select("span.mw-headline:not(#Widget_API,#Widget_hierarchy,#Example,"
                    + "#References),dd:has(a[title^=UIHANDLER ]:eq(0))");
            double total = elements.size();
            int current = 0;
            connectSuccess();
            for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                String text = element.text();
                if ("span".equals(element.tagName())) {
                    if (current > 1) {
                        sb.append("}\n");
                    }
                    sb.append(text).append(" = {\n");
                } else {
                    sb.append("    \"").append(text).append("\",\n");
                }
                current++;
                updateProgress(current / total);
            }
            sb.append("}\n");
            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                writer.print(sb);
            }
        } catch (IOException e) {
            throw new IOException(API_URL, e);
        }
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return getRemoteTimestamp(API_URL);
    }
}
