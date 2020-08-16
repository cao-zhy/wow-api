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
    private static final HashMap<String, String> WIDGET_PARENT = new HashMap<>();

    static {
        WIDGET_PARENT.put("Region", "UIObject");
        WIDGET_PARENT.put("LayeredRegion", "Region");
        WIDGET_PARENT.put("Texture", "LayeredRegion");
        WIDGET_PARENT.put("MaskTexture", "LayeredRegion");
        WIDGET_PARENT.put("Line", "LayeredRegion");
        WIDGET_PARENT.put("ControlPoint", "UIObject");
        WIDGET_PARENT.put("Animation", "UIObject");
        WIDGET_PARENT.put("AnimationGroup", "UIObject");
        WIDGET_PARENT.put("Alpha", "Animation");
        WIDGET_PARENT.put("Scale", "Animation");
        WIDGET_PARENT.put("Translation", "Animation");
        WIDGET_PARENT.put("Rotation", "Animation");
        WIDGET_PARENT.put("Path", "Animation");
        WIDGET_PARENT.put("LineScale", "Animation");
        WIDGET_PARENT.put("LineTranslation", "Animation");
        WIDGET_PARENT.put("TextureCoordTranslation", "Animation");
        WIDGET_PARENT.put("Frame", "Region");
        WIDGET_PARENT.put("FontString", "LayeredRegion");
        WIDGET_PARENT.put("Font", "FontInstance");
        WIDGET_PARENT.put("EditBox", "Frame");
        WIDGET_PARENT.put("MessageFrame", "Frame");
        WIDGET_PARENT.put("SimpleHTML", "Frame");
        WIDGET_PARENT.put("Browser", "Frame");
        WIDGET_PARENT.put("Minimap", "Frame");
        WIDGET_PARENT.put("FogOfWarFrame", "Frame");
        WIDGET_PARENT.put("Checkout", "Frame");
        WIDGET_PARENT.put("ModelScene", "Frame");
        WIDGET_PARENT.put("MovieFrame", "Frame");
        WIDGET_PARENT.put("ColorSelect", "Frame");
        WIDGET_PARENT.put("StatusBar", "Frame");
        WIDGET_PARENT.put("OffScreenFrame", "Frame");
        WIDGET_PARENT.put("Cooldown", "Frame");
        WIDGET_PARENT.put("ScrollFrame", "Frame");
        WIDGET_PARENT.put("UnitPositionFrame", "Frame");
        WIDGET_PARENT.put("GameTooltip", "Frame");
        WIDGET_PARENT.put("Slider", "Frame");
        WIDGET_PARENT.put("WorldFrame", "Frame");
        WIDGET_PARENT.put("ModelSceneActor", "Frame");
        WIDGET_PARENT.put("Model", "Frame");
        WIDGET_PARENT.put("Button", "Frame");
        WIDGET_PARENT.put("POIFrame", "Frame");
        WIDGET_PARENT.put("PlayerModel", "Model");
        WIDGET_PARENT.put("CinematicModel", "PlayerModel");
        WIDGET_PARENT.put("DressUpModel", "PlayerModel");
        WIDGET_PARENT.put("TabardModel", "PlayerModel");
        WIDGET_PARENT.put("CheckButton", "Button");
        WIDGET_PARENT.put("ArchaeologyDigSiteFrame", "POIFrame");
        WIDGET_PARENT.put("QuestPOIFrame", "POIFrame");
        WIDGET_PARENT.put("ScenarioPOIFrame", "POIFrame");

        COPY_FUNCTIONS.put("ScriptObject", new String[]{"Frame", "Animation", "AnimationGroups"});
        COPY_FUNCTIONS.put("FontInstance", new String[]{"FontString", "EditBox", "MessageFrame", "ScrollingMessageFrame",
                "SimpleHTML"});
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
                String parentName = WIDGET_PARENT.get(name);
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
                        String front = "--- " + description + url + "\nfunction ";
                        String back = "(" + (text.indexOf(")") - text.indexOf("(") > 1 ? "..." : "") + ") end\n\n";
                        String name = link.text();
                        sb.append(front).append(name).append(back);

                        // 复制 widget 另一个父类的函数
                        String widgetName = name.split(":")[0];
                        String[] widgets = COPY_FUNCTIONS.get(widgetName);
                        if (widgets != null) {
                            for (String widget : widgets) {
                                sb.append("function ").append(name.replaceFirst(widgetName, widget)).append(back);
                            }
                        }
                    }
                }
            }
            if (sb.length() > 0) {
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                    writer.println(EnumVersionType.PREFIX + getRemoteVersion());
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
