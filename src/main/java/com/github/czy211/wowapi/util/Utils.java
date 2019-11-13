package com.github.czy211.wowapi.util;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Utils {
    /**
     * 部件类型和其父类
     */
    private static final Map<String, String> WIDGETS = new LinkedHashMap<>();

    static {
        WIDGETS.put("Object", "");
        WIDGETS.put("Region", "Object");
        WIDGETS.put("LayeredRegion", "Region");
        WIDGETS.put("ScriptObject", "");
        WIDGETS.put("Frame", "Region");
        WIDGETS.put("ControlPoint", "Object");
        WIDGETS.put("Animation", "Object");
        WIDGETS.put("AnimationGroup", "Object");
        WIDGETS.put("Alpha", "Animation");
        WIDGETS.put("Path", "Animation");
        WIDGETS.put("Scale", "Animation");
        WIDGETS.put("LineScale", "Animation");
        WIDGETS.put("Translation", "Animation");
        WIDGETS.put("LineTranslation", "Animation");
        WIDGETS.put("Rotation", "Animation");
        WIDGETS.put("TextureCoordTranslation", "Animation");
        WIDGETS.put("Texture", "LayeredRegion");
        WIDGETS.put("MaskTexture", "Texture");
        WIDGETS.put("Line", "Texture");
        WIDGETS.put("FontString", "LayeredRegion");
        WIDGETS.put("FontInstance", "");
        WIDGETS.put("Font", "FontInstance");
        WIDGETS.put("EditBox", "FontInstance");
        WIDGETS.put("MessageFrame", "FontInstance");
        WIDGETS.put("ScrollingMessageFrame", "FontInstance");
        WIDGETS.put("SimpleHTML", "FontInstance");
        WIDGETS.put("Browser", "Frame");
        WIDGETS.put("Minimap", "Frame");
        WIDGETS.put("FogOfWarFrame", "Frame");
        WIDGETS.put("Checkout", "Frame");
        WIDGETS.put("ModelScene", "Frame");
        WIDGETS.put("MovieFrame", "Frame");
        WIDGETS.put("ColorSelect", "Frame");
        WIDGETS.put("StatusBar", "Frame");
        WIDGETS.put("OffScreenFrame", "Frame");
        WIDGETS.put("Cooldown", "Frame");
        WIDGETS.put("ScrollFrame", "Frame");
        WIDGETS.put("UnitPositionFrame", "Frame");
        WIDGETS.put("GameTooltip", "Frame");
        WIDGETS.put("Slider", "Frame");
        WIDGETS.put("WorldFrame", "Frame");
        WIDGETS.put("ModelSceneActor", "Frame");
        WIDGETS.put("Model", "Frame");
        WIDGETS.put("Button", "Frame");
        WIDGETS.put("POIFrame", "Frame");
        WIDGETS.put("PlayerModel", "Model");
        WIDGETS.put("CinematicModel", "Model");
        WIDGETS.put("CheckButton", "Button");
        WIDGETS.put("ItemButton", "Button");
        WIDGETS.put("DressUpModel", "Model");
        WIDGETS.put("TabardModel", "Model");
        WIDGETS.put("ArchaeologyDigSite", "POIFrame");
        WIDGETS.put("ScenarioPOIFrame", "POIFrame");
        WIDGETS.put("QuestPOIFrame", "POIFrame");
    }

    /**
     * 判断是否为API元素
     *
     * @param linkHref 链接的字符串内容
     * @return 如果是API元素则返回true，否则返回false
     */
    public static boolean isAPIElement(String linkHref) {
        return linkHref.startsWith("/API_") || pageNotExist(linkHref);
    }

    /**
     * 判断是否为UI函数
     *
     * @param description 函数描述内容
     * @return 如果是UI函数则返回true，否则返回false
     */
    public static boolean isUIFunc(String description) {
        return description.startsWith("UI ");
    }

    /**
     * 判断是否为已删除函数
     *
     * @param description 函数描述内容
     * @return 如果已删除则返回true，否则返回false
     */
    public static boolean isRemovedFunc(String description) {
        return description.startsWith("REMOVED ");
    }

    /**
     * 判断函数页面是否不存在
     *
     * @param linkHref 链接字符串内容
     * @return 如果函数页面不存在则返回true，否则返回false
     */
    public static boolean pageNotExist(String linkHref) {
        return linkHref.startsWith("/index.php");
    }

    /**
     * 将内容写入到文件中
     *
     * @param fileName 待写入的文件
     * @param content 写入的内容
     */
    public static void writeData(String path, String fileName, String content) {
        File file = new File(path + "/wow-api");
        if (file.exists() || file.mkdir()) {
            try (PrintWriter output = new PrintWriter(file.getPath() + "/" + fileName)) {
                output.print(content);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 将内容添加到文件的顶部<br>
     * 函数类型最好应该生成一个新文件，但因为EmmyLua的问题，部件类型和方法不再同一文件时，点击子类调用父类的方法可能会无法正确跳转
     *
     * @param fileName 待写入的文件
     * @param content 写入的内容
     */
    public static void addHeader(String fileName, String content) {
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            int length = (int) raf.length();
            byte[] buff = new byte[length];
            raf.read(buff, 0, length);
            raf.seek(0);
            raf.write(content.getBytes());
            raf.seek(content.length());
            raf.write(buff);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 生成部件类型和方法
     *
     * @param src 部件方法的文件
     * @return 部件类型和方法
     */
    public static String generateWidgetTypesAndFunc(String src) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : WIDGETS.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            result.append("---@class ").append(key);
            if (!"".equals(value)) {
                result.append(":").append(value);
            }
            result.append("\n").append(key).append(" = {}\n\n");
        }

        // 因为EmmyLua的@class注解目前只支持一个父类，所以需要显示定义另一个父类的所有方法。支持多重继承后，可删除
        try (Scanner input = new Scanner(new File(src))) {
            while (input.hasNext()) {
                String line = input.nextLine();
                if (line.startsWith("function ScriptObject:")) {
                    result.append(line.replaceFirst("ScriptObject", "Frame")).append("\n\n");
                    result.append(line.replaceFirst("ScriptObject", "Animation")).append("\n\n");
                    result.append(line.replaceFirst("ScriptObject", "AnimationGroup")).append("\n\n");
                } else if (line.startsWith("function FontInstance:")) {
                    result.append(line.replaceFirst("FontInstance", "FontString")).append("\n\n");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        return result.toString();
    }
}
