package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.constant.WidgetConst;
import com.github.czy211.wowapi.util.Utils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class WidgetApiPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_API";

    public WidgetApiPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        Button btDownload = getBtDownload();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(EnumVersionType.PREFIX).append(getRemoteVersion()).append("\n\n");

            Document document = Jsoup.connect(API_URL).get();
            btDownload.setDisable(false);
            Platform.runLater(() -> btDownload.setText("取消下载"));

            Elements widgetElements = document.select("span.mw-headline:not(#Virtual_frames)");
            Elements elements = document.select("dd");
            int total = widgetElements.size() + elements.size();
            int current = 0;

            for (Element element : widgetElements) {
                current++;
                updateProgress((double) current / total);

                String name = element.text();
                if ("ItemButton".equals(name) || "ScrollingMessageFrame".equals(name)) {
                    continue;
                }
                sb.append("---@class ").append(name);
                String parentName = WidgetConst.WIDGET_PARENT.get(name);
                if (parentName != null) {
                    sb.append(":").append(parentName);
                }
                sb.append("\n").append(name).append(" = {}\n\n");
            }

            for (Element element : elements) {
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                if (text.startsWith("UI ") || text.startsWith("DEPRECATED ") || text.startsWith("REMOVED ")) {
                    continue;
                }
                Element link = element.selectFirst("a");
                if (link != null) {
                    String title = link.attr("title");
                    if (title != null && title.startsWith("API ")) {
                        String description = text.replaceAll("\\[", "{").replaceAll("]", "}");
                        String url = "";
                        if (!title.endsWith("(page does not exist)")) {
                            url = "\n---\n--- [" + LinkConst.WIKI_BASE + link.attr("href") + "]";
                        }
                        String name = link.text();
                        sb.append("--- ").append(description).append(url).append("\nfunction ").append(name)
                                .append("(");
                        if (text.indexOf(")") - text.indexOf("(") > 1) {
                            sb.append("...");
                        }
                        sb.append(") end\n\n");

                        // 复制 widget 另一个父类的函数
                        String[] names = name.split(":");
                        String widgetName = names[0];
                        String functionName = names[1];
                        String[] widgets = WidgetConst.COPY_FUNCTION.get(widgetName);
                        if (widgets != null) {
                            for (String widget : widgets) {
                                sb.append("---@see ").append(widgetName).append("#").append(functionName)
                                        .append("\nfunction ").append(widget).append(":").append(functionName)
                                        .append("(");
                                if (text.indexOf(")") - text.indexOf("(") > 1) {
                                    sb.append("...");
                                }
                                sb.append(") end\n\n");
                            }
                        }
                    }
                }
            }
            if (sb.length() > 0) {
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
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
