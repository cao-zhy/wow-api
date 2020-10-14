package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.model.Template;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WidgetsPane extends BaseApiPane {
    private static final ArrayList<String> TEMPLATE_TAGS = new ArrayList<>();
    private static final ArrayList<String> WIDGET_TAGS = new ArrayList<>();
    private static final Pattern FUNCTION_DEFINITION_PATTERN1 = Pattern.compile("function[ \t]+(\\w+):\\w+[ \t]*\\(.*"
            + "\\)");
    private static final Pattern FUNCTION_DEFINITION_PATTERN2 = Pattern.compile("_G\\.(\\w+)[ \t]*=[ \t]*(\\w+)$");
    private static final String CHILD_NAME_PREFIX = "$parent";
    private static final Map<String, Template> TEMPLATE_MAP = new HashMap<>();
    private Label lbBicName;
    private TextField tfBicPath;
    private Button btSelect;
    private double total;
    private long current;

    static {
        TEMPLATE_TAGS.addAll(Arrays.asList("Frame", "ContainedAlertFrame", "DropDownToggleButton", "ScrollBarButton"));
        TEMPLATE_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Frame")));
        TEMPLATE_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Model")));
        TEMPLATE_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("PlayerModel")));
        TEMPLATE_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Button")));
        TEMPLATE_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("POIFrame")));

        WIDGET_TAGS.addAll(TEMPLATE_TAGS);
        WIDGET_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("UIObject")));
        WIDGET_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("LayeredRegion")));
        WIDGET_TAGS.addAll(Arrays.asList(WidgetHierarchyPane.WIDGETS.get("Animation")));
    }

    public WidgetsPane(String name, EnumVersionType versionType) {
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
                            Elements elements = document.select("[intrinsic=true],[name]:not([name^=$])[virtual=true]");
                            for (Element element : elements) {
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                String tagName = element.tagName();
                                if (TEMPLATE_TAGS.contains(tagName)) {
                                    String name = element.attr("name");
                                    Template template = new Template(name);
                                    String inherits = element.attr("inherits");
                                    if (!"".equals(inherits)) {
                                        template.getInterfaces().addAll(Arrays.asList(inherits.split(", |,")));
                                    }
                                    String mixins = element.attr("mixin") + element.attr("secureMixin");
                                    if (!"".equals(mixins)) {
                                        template.getInterfaces().addAll(Arrays.asList(mixins.split(", |,")));
                                    }
                                    Elements els = element.select("[name^=$parent]:not([parentKey])");
                                    if (els.size() > 0) {
                                        for (Element el : els) {
                                            String widgetName = processName(el);
                                            template.getWidgets().put(widgetName, el);
                                        }
                                    }
                                    TEMPLATE_MAP.put(name, template);
                                    // 添加有 parentKey 属性子元素的 virtual frame 和 intrinsic frame
                                    if (hasChildParentKey(element)) {
                                        sb.append("---@class ").append(name).append(":").append(tagName).append("\n")
                                                .append(name).append(" = {\n");
                                        appendParentKeys(sb, element, 1, name, name);
                                        sb.append("}\n\n");
                                    }
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

    public void appendWidgets(File path, StringBuilder sb) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    appendWidgets(inPath, sb);
                } else {
                    String filename = inPath.getName();
                    try {
                        InputStream inputStream = new FileInputStream(inPath);
                        if (filename.endsWith(".xml")) {
                            Document document = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
                            Elements elements = document.select("[name]:not([name^=$],[virtual=true],[intrinsic=true],"
                                    + "[parentKey])");
                            for (Element element : elements) {
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }
                                if (WIDGET_TAGS.contains(processTagName(element.tagName()))) {
                                    appendWidget(sb, element);
                                    for (Element el : element.children().select("[name^=$parent]:not([parentKey])")) {
                                        if (WIDGET_TAGS.contains(processTagName(el.tagName()))) {
                                            appendWidget(sb, el);
                                        }
                                    }
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

    public void appendWidget(StringBuilder sb, Element element) {
        String name = processName(element);
        if (hasChildParentKey(element)) {
            String tagName = processTagName(element.tagName());
            if (!name.equals(tagName)) {
                sb.append("---class ").append(name).append(":").append(tagName).append("\n");
            }
            sb.append(name).append(" = {\n");
            appendParentKeys(sb, element, 1, name, element.attr("name"));
            sb.append("}\n\n");
        }
        appendTypes(sb, element);
        sb.append(name).append(" = {}\n\n");

        // 添加 interfaces 中的子 widget
        appendInterfacesChildWidget(sb, name, Arrays.asList(element.attr("inherits").split(", |,")));
    }

    public void appendInterfacesChildWidget(StringBuilder sb, String name, List<String> inherits) {
        for (String inherit : inherits) {
            Template template = TEMPLATE_MAP.get(inherit);
            if (template != null) {
                for (Map.Entry<String, Element> entry : template.getWidgets().entrySet()) {
                    String newName = entry.getKey().replace(CHILD_NAME_PREFIX, name);
                    Element el = entry.getValue().attr("name", newName);
                    appendWidget(sb, el);
                }
                appendInterfacesChildWidget(sb, name, template.getInterfaces());
            }
        }
    }

    public void appendTypes(StringBuilder sb, Element element) {
        String tagName = element.tagName();
        sb.append("---@type ").append(processTagName(tagName));
        if ("ScrollingMessageFrame".equals(tagName)) {
            sb.append("|FontStance");
        }
        Template template = TEMPLATE_MAP.get(tagName);
        if (template != null) {
            appendInterfaces(sb, template.getInterfaces());
        }
        if (TEMPLATE_TAGS.contains(tagName)) {
            String inherits = element.attr("inherits");
            if (!"".equals(inherits)) {
                appendInterfaces(sb, Arrays.asList(inherits.split(", |,")));
            }
        }
        String mixins = element.attr("mixin") + element.attr("secureMixin");
        if (!"".equals(mixins)) {
            for (String mixin : mixins.split(", |,")) {
                sb.append("|").append(mixin);
            }
        }
        sb.append("\n");
    }

    public void appendInterfaces(StringBuilder sb, List<String> inherits) {
        for (String inherit : inherits) {
            sb.append("|").append(inherit);
            Template template = TEMPLATE_MAP.get(inherit);
            if (template != null) {
                appendInterfaces(sb, template.getInterfaces());
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

                boolean hasChildParentKey = hasChildParentKey(el);
                String name = el.attr("parentKey");
                String className = prefix;
                if (hasChildParentKey) {
                    className += "_" + name;
                    sb.append("---@class ").append(className).append(":").append(processTagName(el.tagName()))
                            .append("\n");
                } else {
                    appendTypes(sb, el);
                }

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

                if (hasChildParentKey) {
                    for (int i = 0; i < numBlank; i++) {
                        sb.append("    ");
                    }
                    appendTypes(sb, el);
                    for (int i = 0; i < numBlank; i++) {
                        sb.append("    ");
                    }
                    sb.append(name).append(" = {},\n");
                }
            }
        }
        for (Element el : element.children()) {
            if (!elements.contains(el)) {
                appendParentKeys(sb, el, numBlank, prefix, templateName);
            }
        }
    }

    public String processName(Element element) {
        String name = element.attr("name");
        int index = -1;
        while (name.startsWith(CHILD_NAME_PREFIX)) {
            for (int i = index + 1; i < element.parents().size(); i++) {
                Element parent = element.parents().get(i);
                if ("true".equals(parent.attr("virtual"))) {
                    return name;
                }
                String parentName = parent.attr("name");
                if (!"".equals(parentName)) {
                    // 使用父元素的名称替换“$parent”
                    name = name.replace(CHILD_NAME_PREFIX, parentName);
                    index = i;
                    break;
                }
            }
        }
        return name;
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
        StringBuilder templateSb = new StringBuilder();
        HashSet<String> set = new HashSet<>();
        total = getFileSize(inPath) * 2;
        current = 0;
        connectSuccess();
        appendTemplates(inPath, templateSb, set);
        if (templateSb.length() > 0) {
            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + "Templates.lua", "UTF-8")) {
                writer.print(templateSb);
            }
        }
        StringBuilder widgetSb = new StringBuilder();
        appendWidgets(inPath, widgetSb);
        if (widgetSb.length() > 0) {
            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                writer.print(widgetSb);
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
