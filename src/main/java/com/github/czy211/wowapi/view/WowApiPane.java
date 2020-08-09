package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.util.Utils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class WowApiPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/World_of_Warcraft_API";

    public WowApiPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        Button btDownload = getBtDownload();
        try {
            StringBuilder sb = new StringBuilder();
            // 添加版本信息
            sb.append(EnumVersionType.PREFIX).append(getRemoteVersion()).append("\n\n");

            Document document = Jsoup.connect(API_URL).get();
            btDownload.setDisable(false);
            Platform.runLater(() -> btDownload.setText("取消下载"));

            Elements elements = document.select("dd, span#Classic_Specific_Functions");
            int total = elements.size();
            int current = 0;
            for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                if (text.startsWith("UI ") || text.startsWith("DEPRECATED ") || text.startsWith("REMOVED ")) {
                    // 跳过的函数
                    continue;
                } else if ("span".equals(element.tagName())) {
                    // 遍历完成
                    break;
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
