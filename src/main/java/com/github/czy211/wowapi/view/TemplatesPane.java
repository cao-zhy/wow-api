package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.util.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplatesPane extends BaseApiPane {
    private static final ArrayList<String> FRAMES = new ArrayList<>();
    private static final Pattern FUNCTION_DEFINITION_PATTERN1 = Pattern.compile("function\\s+(\\w+(\\.\\w+)*)[.:]\\w+"
            + "\\s*\\(.*\\)");
    private static final Pattern FUNCTION_DEFINITION_PATTERN2 = Pattern.compile("_G\\.(\\w+)\\s*=\\s*(\\w+)$");
    private Label lbBicName;
    private TextField tfBicPath;
    private Button btSelect;
    private double total;
    private long current;

    static {
        FRAMES.addAll(Arrays.asList("Frame", "ContainedAlertFrame", "DropDownToggleButton", "ScrollBarButton"));
        FRAMES.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Frame")));
        FRAMES.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Model")));
        FRAMES.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("PlayerModel")));
        FRAMES.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Button")));
        FRAMES.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("POIFrame")));
    }

    public TemplatesPane(String name, EnumVersionType versionType) {
        super(name, versionType);
        current = 0;
        lbBicName = new Label("BlizzardInterfaceCode");
        tfBicPath = new TextField();
        btSelect = new Button("选择文件夹");
        HBox topNode = new HBox(5, lbBicName, tfBicPath, btSelect);

        setTop(topNode);

        topNode.setAlignment(Pos.CENTER);
        HBox.setHgrow(tfBicPath, Priority.ALWAYS);
        setMargin(topNode, new Insets(0, 0, 5, 0));
        tfBicPath.setEditable(false);
        tfBicPath.setFocusTraversable(false);
        String bicPath = Utils.getBicPath();
        if (bicPath == null) {
            getBtDownload().setDisable(true);
        } else {
            tfBicPath.setText(bicPath);
        }
    }

    public void appendTemplates(File path, StringBuilder sb, HashSet<String> set) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    appendTemplates(inPath, sb, set);
                } else {
                    String filename = inPath.getName();
                    try {
                        InputStream inputStream = new FileInputStream(inPath);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                                StandardCharsets.UTF_8));
                        if (filename.endsWith(".lua")) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                line = line.trim();
                                String name = "";
                                Matcher matcher = FUNCTION_DEFINITION_PATTERN1.matcher(line);
                                if (matcher.find()) {
                                    name = matcher.group(1);
                                }
                                if (!"".equals(name) && !set.contains(name)) {
                                    set.add(name);
                                    sb.append("---@class ").append(name).append("\n").append(name).append(" = {}\n\n");
                                }
                                // 添加 TickerPrototype 函数，因为暴雪使用 local 定义 TickerPrototype
                                if ("TickerPrototype".equals(name)) {
                                    sb.append(line).append(" end\n\n");
                                }
                                matcher = FUNCTION_DEFINITION_PATTERN2.matcher(line);
                                if (matcher.find() && matcher.group(1).equals(matcher.group(2))) {
                                    // 添加 Blizzard_CombatLog.lua 中使用 _G.xxx = xxx 方式定义的全局函数
                                    sb.append("function ").append(matcher.group(1)).append("(...) end\n\n");
                                }
                            }
                        } else if (filename.endsWith("xml")) {
                            Document document = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
                            // 添加有 parentKey 属性子元素的 virtual frame 和 intrinsic frame
                            Elements elements = document.select("[intrinsic=true]:has([parentKey]:not([virtual=true])),"
                                    + "[name]:not([name^=$parent])[virtual=true]:has([parentKey]:not([virtual=true]))");
                            for (Element element : elements) {
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                String tagName = element.tagName();
                                if (FRAMES.contains(tagName) && hasChildParentKey(element)) {
                                    String name = element.attr("name");
                                    sb.append("---@class ").append(name).append(":").append(tagName).append("\n")
                                            .append(name).append(" = {\n");
                                    appendParentKeys(sb, element, 1, name, name);
                                    sb.append("}\n\n");
                                }
                            }
                        }
                        current += getFileSize(inPath);
                        updateProgress(current / total);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean hasChildParentKey(Element element) {
        Elements elements = element.children().select("[parentKey]");
        for (Element el : elements) {
            if (isChildParentKey(el, element.attr("name"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isChildParentKey(Element element, String templateName) {
        for (int i = 0; i < element.parents().size(); i++) {
            Element parent = element.parents().get(i);
            String name = parent.attr("name");
            String parentKey = parent.attr("parentKey");
            if (!"".equals(parentKey)) {
                return true;
            }
            if (!"".equals(name) && !templateName.equals(name)) {
                return false;
            }
            if (templateName.equals(name)) {
                return true;
            }
        }
        return true;
    }

    public void appendParentKeys(StringBuilder sb, Element element, int numBlank, String prefix, String templateName) {
        Elements elements = element.select(">[parentKey]:not([virtual=true])");
        for (Element el : elements) {
            if (isChildParentKey(el, templateName)) {
                for (int i = 0; i < numBlank; i++) {
                    sb.append("    ");
                }

                String name = el.attr("parentKey");
                String className = prefix;
                if (hasChildParentKey(el)) {
                    className += "_" + name;
                    sb.append("---@class ").append(className).append(":");
                } else {
                    sb.append("---@type ");
                }
                sb.append(processTagName(el.tagName())).append("\n");

                for (int i = 0; i < numBlank; i++) {
                    sb.append("    ");
                }

                if (name.contains("-")) {
                    name = "[\"" + name + "\"]";
                }
                sb.append(name).append(" = {\n");
                appendParentKeys(sb, el, numBlank + 1, className, templateName);

                for (int i = 0; i < numBlank; i++) {
                    sb.append("    ");
                }
                sb.append("},\n");
            }
        }
        for (Element el : element.children()) {
            appendParentKeys(sb, el, numBlank, prefix, templateName);
        }
    }

    public String processTagName(String tagName) {
        if (tagName.endsWith("Texture") && !"MaskTexture".equals(tagName)) {
            tagName = "Texture";
        } else if ("ButtonText".equals(tagName)) {
            tagName = "FontString";
        }
        return tagName;
    }

    public long getFileSize(File filepath) {
        if (filepath.isFile()) {
            return filepath.length();
        } else if (filepath.isDirectory()) {
            long total = 0;
            File[] files = filepath.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        total += file.length();
                    } else if (file.isDirectory()) {
                        total += getFileSize(file);
                    }
                }
            }
            return total;
        }
        return 0;
    }

    @Override
    public void download() throws IOException {
        File inPath = new File(Utils.getBicPath());
        StringBuilder sb = new StringBuilder();
        HashSet<String> set = new HashSet<>();
        total = getFileSize(inPath);
        connectSuccess();
        appendTemplates(inPath, sb, set);
        if (sb.length() > 0) {
            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                writer.print(sb);
            }
        }
    }

    @Override
    public long getRemoteVersion() {
        return 0;
    }

    public Label getLbBicName() {
        return lbBicName;
    }

    public void setLbBicName(Label lbBicName) {
        this.lbBicName = lbBicName;
    }

    public TextField getTfBicPath() {
        return tfBicPath;
    }

    public void setTfBicPath(TextField tfBicPath) {
        this.tfBicPath = tfBicPath;
    }

    public Button getBtSelect() {
        return btSelect;
    }

    public void setBtSelect(Button btSelect) {
        this.btSelect = btSelect;
    }
}
