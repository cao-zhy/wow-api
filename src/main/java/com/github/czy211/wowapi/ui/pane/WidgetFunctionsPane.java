package com.github.czy211.wowapi.ui.pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WidgetFunctionsPane extends WikiPane {
    private static final String name = "Widget_Functions";
    private static final String extension = ".lua";
    private static final String url = BASE_URI + "/wiki/Widget_API";
    private static final WidgetFunctionsPane pane = new WidgetFunctionsPane();
    private final String widgets;
    private final HashMap<String, String[]> functionsToCopy = new HashMap<>();
    
    private WidgetFunctionsPane() {
        super(name, extension, url);
        HashMap<String, String[]> functionHierarchy = new HashMap<>();
        functionHierarchy.put("UIObject", new String[]{"ParentedObject", "FontInstance"});
        functionHierarchy.put("ParentedObject", new String[]{"ControlPoint", "Animation", "AnimationGroup", "Region"});
        functionHierarchy.put("FontInstance", new String[]{"Font"});
        functionHierarchy.put("Animation", new String[]{"Alpha", "Path", "Scale", "LineScale", "Translation",
                "LineTranslation", "Rotation", "TextureCoordTranslation"});
        functionHierarchy.put("Region", new String[]{"LayeredRegion", "Frame"});
        functionHierarchy.put("LayeredRegion", new String[]{"FontString", "Texture", "MaskTexture", "Line"});
        functionHierarchy.put("Frame", new String[]{"Button", "Model", "ModelScene", "ModelSceneActor", "POIFrame",
                "FogOfWarFrame", "UnitPositionFrame", "ColorSelect", "Cooldown", "GameTooltip", "ScrollFrame", "Slider",
                "StatusBar", "Minamap", "WorldFrame", "MovieFrame", "Browser", "Checkout", "OffScreenFrame", "EditBox",
                "MessageFrame", "SimpleHTML", "ScrollingMessageFrame"});
        functionHierarchy.put("Button", new String[]{"CheckButton", "ItemButton", "EventButton", "ContainedAlertFrame",
                "DropDownToggleButton"});
        functionHierarchy.put("Model", new String[]{"PlayerModel"});
        functionHierarchy.put("PlayerModel", new String[]{"CinematicModel", "DressUpModel", "TabardModel"});
        functionHierarchy.put("POIFrame", new String[]{"ArchaeologyDigSiteFrame", "QuestPOIFrame", "ScenarioPOIFrame"});
        String[] tops = {"UIObject", "ScriptObject"};
        StringBuilder sb = new StringBuilder();
        for (String name : tops) {
            sb.append("---@class ").append(name).append("\n").append(name).append(" = {}\n\n");
        }
        for (Map.Entry<String, String[]> entry : functionHierarchy.entrySet()) {
            String parentName = entry.getKey();
            for (String name : entry.getValue()) {
                sb.append("---@class ").append(name).append(":").append(parentName).append("\n").append(name)
                        .append(" = {}\n\n");
            }
        }
        widgets = sb.toString();
        
        functionsToCopy.put("ScriptObject", new String[]{"Animation", "AnimationGroup", "Frame"});
        functionsToCopy.put("FontInstance", new String[]{"FontString", "EditBox", "MessageFrame", "SimpleHTML",
                "ScrollingMessageFrame"});
        functionsToCopy.put("Scale", new String[]{"LineScale"});
        functionsToCopy.put("Translation", new String[]{"LineTranslation"});
        functionsToCopy.put("Texture", new String[]{"MaskTexture", "Line"});
    }
    
    public static WidgetFunctionsPane getInstance() {
        return pane;
    }
    
    @Override
    public void download() {
        resolve(widgets, "dd:not(:matches(^REMOVED)):has(a[title^=API ])", null, functionsToCopy);
    }
    
    @Override
    public String getRemoteVersion() throws IOException {
        return getPageDateTime();
    }
}
