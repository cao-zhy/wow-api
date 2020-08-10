package com.github.czy211.wowapi.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class WidgetConst {
    public static final HashMap<String, String> WIDGET_PARENT = new HashMap<>();
    public static final HashMap<String, String[]> COPY_FUNCTIONS = new HashMap<>();
    public static final HashMap<String, String> INTRINSIC_MIXIN = new HashMap<>();
    public static final HashSet<String> EXCLUDE_TAGS = new HashSet<>(Arrays.asList("Screen", "LCDBitmap", "Binding",
            "LCDText", "Attribute", "ModelFFX", "LCDSubscreen", "ColorWheelTexture"));

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

        INTRINSIC_MIXIN.put("DropDownToggleButton", "DropDownToggleButtonMixin");
        INTRINSIC_MIXIN.put("ContainedAlertFrame", "ContainedAlertFrameMixin");
        INTRINSIC_MIXIN.put("ItemButton", "ItemButtonMixin");
        INTRINSIC_MIXIN.put("ScrollingMessageFrame", "ScrollingMessageFrameMixin");
    }
}