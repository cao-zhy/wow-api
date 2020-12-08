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
import java.util.HashMap;

public class WidgetApiPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_API";
    private static final HashMap<String, String[]> COPY_FUNCTIONS = new HashMap<>();

    static {
        COPY_FUNCTIONS.put("ScriptObject", new String[]{"Animation", "AnimationGroup", "Frame"});
        COPY_FUNCTIONS.put("FontInstance", new String[]{"FontString", "EditBox", "MessageFrame", "SimpleHTML",
                "ScrollingMessageFrame"});
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
            sb.append(EnumVersionType.PREFIX).append(getRemoteVersion()).append("\n\n")
                    .append(WidgetHierarchyPane.WIDGET_HIERARCHY);
            Document document = Jsoup.connect(API_URL).get();
            Elements elements = document.select("dd:not(:matches(^REMOVED )):has(a[title^=API ]:eq(0))");
            double total = elements.size();
            int current = 0;
            connectSuccess();
            for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                StringBuilder after = appendFunction(sb, element);
                // 复制函数
                String funcName = element.selectFirst("a").text();
                String name = funcName.split(":")[0];
                String[] widgets = COPY_FUNCTIONS.get(name);
                if (widgets != null) {
                    for (String newName : widgets) {
                        sb.append("function ").append(funcName.replaceFirst(name, newName)).append(after);
                    }
                }
                current++;
                updateProgress(current / total);
            }
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
