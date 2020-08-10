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

            Elements elements = document.select("span.mw-headline:not(#Widget_API, #Widget_hierarchy, #Example, "
                    + "#References), dd:not(:has(.disambig-for.noexcerpt, ul))");
            int total = elements.size();
            int current = 0;
            boolean closingBrace = false;
            for (Element element : elements) {
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                if (text.startsWith("UI ") || text.startsWith("DEPRECATED ") || text.startsWith("REMOVED ")) {
                    continue;
                }
                if ("span".equals(element.tagName())) {
                    if (closingBrace) {
                        sb.append("    },\n");
                    }
                    sb.append("    ").append(text).append(" = {\n");
                    closingBrace = true;
                } else {
                    Element link = element.selectFirst("a");
                    if (link != null) {
                        String title = link.attr("title");
                        if (title != null && title.startsWith("UIHANDLER ")) {
                            sb.append("        \"").append(text).append("\",\n");
                            closingBrace = true;
                        }
                    }
                }
            }
            if (sb.length() > 0) {
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                    writer.println(EnumVersionType.PREFIX + getRemoteVersion());
                    writer.println("\nlocal widgetScriptHandlers = {\n");
                    writer.println(sb);
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
