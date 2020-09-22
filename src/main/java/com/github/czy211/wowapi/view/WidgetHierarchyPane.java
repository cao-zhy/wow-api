package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.util.Utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class WidgetHierarchyPane extends BaseApiPane {
    public static final String[] ROOT_WIDGETS = {"UIObject", "ScriptObject", "FontInstance"};
    public static final HashMap<String, String[]> WIDGETS = new HashMap<>();
    public static String WIDGET_HIERARCHY;

    static {
        WIDGETS.put("UIObject", new String[]{"Region", "AnimationGroup", "Animation", "ControlPoint"});
        WIDGETS.put("FontInstance", new String[]{"Font"});
        WIDGETS.put("Region", new String[]{"LayeredRegion", "Frame"});
        WIDGETS.put("LayeredRegion", new String[]{"FontString", "Texture", "MaskTexture", "Line"});
        WIDGETS.put("Animation", new String[]{"Alpha", "Path", "Scale", "LineScale", "Translation", "LineTranslation",
                "Rotation", "TextureCoordTranslation"});
        WIDGETS.put("Frame", new String[]{"Model", "Button", "POIFrame", "Browser", "Minimap", "FogOfWarFrame",
                "Checkout", "ModelScene", "MovieFrame", "ColorSelect", "StatusBar", "OffScreenFrame", "Cooldown",
                "ScrollFrame", "UnitPositionFrame", "GameTooltip", "Slider", "WorldFrame", "ModelSceneActor",
                "EditBox", "MessageFrame", "SimpleHTML", "ScrollingMessageFrame",});
        WIDGETS.put("Model", new String[]{"PlayerModel"});
        WIDGETS.put("PlayerModel", new String[]{"CinematicModel", "DressUpModel", "TabardModel"});
        WIDGETS.put("Button", new String[]{"CheckButton", "ItemButton"});
        WIDGETS.put("POIFrame", new String[]{"ArchaeologyDigSiteFrame", "QuestPOIFrame", "ScenarioPOIFrame"});

        StringBuilder sb = new StringBuilder();
        for (String name : ROOT_WIDGETS) {
            sb.append("---@class ").append(name).append("\n").append(name).append(" = {}\n\n");
        }
        for (Map.Entry<String, String[]> entry : WIDGETS.entrySet()) {
            String parentName = entry.getKey();
            String[] names = entry.getValue();
            for (String name : names) {
                sb.append("---@class ").append(name).append(":").append(parentName).append("\n").append(name)
                        .append(" = {}\n\n");
            }
        }
        WIDGET_HIERARCHY = sb.toString();
    }

    public WidgetHierarchyPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        String urlStr = "https://gamepedia.cursecdn.com/wowpedia/c/ca/Widget_Hierarchy.png";
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            double total = connection.getContentLength();
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(Utils.getDownloadPath()
                         + getName()))) {
                connectSuccess();

                byte[] data = new byte[8192];
                int current = 0;
                int length;
                while ((length = in.read(data)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    out.write(data, 0, length);
                    out.flush();
                    current += length;
                    updateProgress(current / total);
                }
            }
        } catch (IOException e) {
            throw new IOException(urlStr, e);
        }
    }

    @Override
    public long getRemoteVersion() {
        return 0;
    }
}
