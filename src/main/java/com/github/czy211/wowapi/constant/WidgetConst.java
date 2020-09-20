package com.github.czy211.wowapi.constant;

import java.util.HashMap;
import java.util.Map;

public class WidgetConst {
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
                "EditBox", "MessageFrame", "SimpleHTML"});
        WIDGETS.put("Model", new String[]{"PlayerModel"});
        WIDGETS.put("PlayerModel", new String[]{"CinematicModel", "DressUpModel", "TabardModel"});
        WIDGETS.put("Button", new String[]{"CheckButton"});
        WIDGETS.put("POIFrame", new String[]{"ArchaeologyDigSiteFrame", "QuestPOIFrame", "ScenarioPOIFrame"});

        StringBuilder sb = new StringBuilder();
        for (String name : WidgetConst.ROOT_WIDGETS) {
            sb.append("---@class ").append(name).append("\n").append(name).append(" = {}\n\n");
        }
        for (Map.Entry<String, String[]> entry : WidgetConst.WIDGETS.entrySet()) {
            String parentName = entry.getKey();
            String[] names = entry.getValue();
            for (String name : names) {
                sb.append("---@class ").append(name).append(":").append(parentName).append("\n").append(name)
                        .append(" = {}\n\n");
            }
        }
        WIDGET_HIERARCHY = sb.toString();

        WIDGETS.put("Intrinsic", new String[]{"DropDownToggleButton", "ContainedAlertFrame", "ItemButton",
                "ScrollBarButton", "ScrollingMessageFrame"});
    }
}
