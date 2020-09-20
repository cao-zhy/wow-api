package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.WidgetConst;
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

public class GlobalVariablesPane extends BaseApiPane {
    private static final String CHILD_NAME_PREFIX = "$parent";
    private static final ArrayList<String> FRAMES = new ArrayList<>();
    private static final Map<String, Template> TEMPLATE_MAP = new HashMap<>();

    static {
        FRAMES.add("Frame");
        FRAMES.addAll(Arrays.asList(WidgetConst.WIDGETS.get("Frame")));
        FRAMES.addAll(Arrays.asList(WidgetConst.WIDGETS.get("Model")));
        FRAMES.addAll(Arrays.asList(WidgetConst.WIDGETS.get("PlayerModel")));
        FRAMES.addAll(Arrays.asList(WidgetConst.WIDGETS.get("Button")));
        FRAMES.addAll(Arrays.asList(WidgetConst.WIDGETS.get("POIFrame")));
        FRAMES.addAll(Arrays.asList(WidgetConst.WIDGETS.get("Intrinsic")));
    }

    private Label lbBicName;
    private TextField tfBicPath;
    private Button btSelect;

    public GlobalVariablesPane(String name, EnumVersionType versionType) {
        super(name, versionType);
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

    public void parseFiles(File path, StringBuilder sb, HashSet<String> set, Progress progress) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    parseFiles(inPath, sb, set, progress);
                } else {
                    String filename = inPath.getName();
                    try {
                        InputStream inputStream = new FileInputStream(inPath);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                                StandardCharsets.UTF_8));
                        if (filename.endsWith(".lua")) {
                            // 解析 lua 文件
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                String name = "";
                                if (line.matches("function .+[.:].+\\(.*\\)")) {
                                    name = getFunctionParentName(line);
                                } else if (line.matches("\\w+ = CreateFromMixins\\(.*\\);")) {
                                    name = line.substring(0, line.indexOf("=") - 1);
                                } else if (line.matches("_G\\..+=.+")) {
                                    // 添加 Blizzard_CombatLog.lua 中使用 _G.xxx = xxx 方式定义的全局函数
                                    int index = line.indexOf("=");
                                    String leftName = line.substring(3, index - 1);
                                    String rightName = line.substring(index + 2);
                                    if (leftName.equals(rightName)) {
                                        sb.append("function ").append(leftName).append("(...) end\n\n");
                                    }
                                }
                                if (!"".equals(name) && !set.contains(name)) {
                                    set.add(name);
                                    sb.append("---@class ").append(name).append("\n").append(name)
                                            .append(" = {}\n\n");
                                }
                                // 添加 TickerPrototype 函数，因为暴雪使用 local 定义 TickerPrototype
                                if ("TickerPrototype".equals(name)) {
                                    sb.append(line).append(" end\n\n");
                                }
                            }
                        } else if (filename.endsWith(".xml")) {
                            // 解析 xml 文件
                            Document document = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
                            Elements elements = document.select("Ui>[name]:not([virtual=true],[intrinsic=true])");
                            for (Element element : elements) {
                                String tagName = element.tagName();
                                if (FRAMES.contains(tagName)) {
                                    appendWidget(sb, element);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progress.current += getFileSize(inPath);
                    updateProgress(progress.getProgress());
                }
            }
        }
    }

    public void appendWidget(StringBuilder sb, Element element) {
        appendTypes(sb, element);
        String name = handleName(element);
        sb.append(name).append(" = {\n");
        appendParentKeys(sb, element, 1);
        sb.append("}\n\n");

        String inherits = element.attr("inherits");
        if (!"".equals(inherits)) {
            String[] inheritList = inherits.split(", |,");
            for (String inherit : inheritList) {
                Template template = TEMPLATE_MAP.get(inherit);
                if (template != null) {
                    for (Map.Entry<String, Element> entry : template.getWidgets().entrySet()) {
                        String newName = entry.getKey().replace(CHILD_NAME_PREFIX, name);
                        Element el = entry.getValue().attr("name", newName);
                        appendWidget(sb, el);
                    }
                }
            }
        }

        for (Element el : element.children().select("[name]:not([virtual=true],[parentKey])")) {
            appendWidget(sb, el);
        }
    }

    public void parseForTemplates(File path, StringBuilder sb, Progress progress) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    parseForTemplates(inPath, sb, progress);
                } else {
                    String filename = inPath.getName();
                    if (filename.endsWith(".xml")) {
                        try {
                            InputStream in = new FileInputStream(inPath);
                            Document document = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
                            Elements elements = document.select("[intrinsic=true],[name]:not([name^=$parent])"
                                    + "[virtual=true]");
                            for (Element element : elements) {
                                String tagName = element.tagName();
                                if (FRAMES.contains(tagName)) {
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
                                    Elements els = element.select("[name^=$parent]:not([virtual=true],[parentKey])");
                                    if (els.size() > 0) {
                                        for (Element el : els) {
                                            String widgetName = handleName(el);
                                            template.getWidgets().put(widgetName, el);
                                        }
                                    }
                                    TEMPLATE_MAP.put(name, template);
                                    sb.append("---@class ").append(name);
                                    if ("true".equals(element.attr("intrinsic"))) {
                                        sb.append(":").append(element.tagName());
                                    }
                                    sb.append("\n").append(name).append(" = {\n");
                                    appendParentKeys(sb, element, 1);
                                    sb.append("}\n\n");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    progress.current += getFileSize(inPath);
                    updateProgress(progress.getProgress());
                }
            }
        }
    }

    public String handleName(Element element) {
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
                    // 使用父元素的名称替换 “$parent”
                    name = name.replace(CHILD_NAME_PREFIX, parentName);
                    index = i;
                    break;
                }
            }
        }
        return name;
    }

    public void appendParentKeys(StringBuilder sb, Element element, int numBlank) {
        Elements elements = element.select(">[parentKey]:not([virtual=true])");
        for (Element el : elements) {
            for (int i = 0; i < numBlank; i++) {
                sb.append("    ");
            }

            appendTypes(sb, el);

            for (int i = 0; i < numBlank; i++) {
                sb.append("    ");
            }

            String name = el.attr("parentKey");
            if (name.contains("-")) {
                name = "[\"" + name + "\"]";
            }
            sb.append(name).append(" = {\n");
            appendParentKeys(sb, el, numBlank + 1);

            for (int i = 0; i < numBlank; i++) {
                sb.append("    ");
            }
            sb.append("},\n");
        }
        for (Element el : element.children()) {
            appendParentKeys(sb, el, numBlank);
        }
    }

    public void appendTypes(StringBuilder sb, Element element) {
        String tagName = element.tagName();
        sb.append("---@type ").append(handleTagName(tagName));

        Template template = TEMPLATE_MAP.get(tagName);
        if (template != null) {
            appendInterfaces(sb, template.getInterfaces());
        }
        if (FRAMES.contains(tagName)) {
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

    public String handleTagName(String tagName) {
        if (tagName.endsWith("Texture") && !"MaskTexture".equals(tagName)) {
            tagName = "Texture";
        } else if ("ButtonText".equals(tagName)) {
            tagName = "FontString";
        } else if ("ScrollingMessageFrame".equals(tagName)) {
            tagName += "|FontInstance";
        }
        return tagName;
    }

    public long getFileSize(File filepath) {
        if (filepath.isFile()) {
            return filepath.length();
        }
        long total = 0;
        File[] files = filepath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    total += file.length();
                } else {
                    total += getFileSize(file);
                }
            }
        }
        return total;
    }

    public String getFunctionParentName(String line) {
        line = line.trim();
        int index = line.indexOf(":");
        if (index == -1) {
            index = line.indexOf(".");
        }
        return line.substring(9, index);
    }

    @Override
    public void download() throws IOException {
        File inPath = new File(Utils.getBicPath());
        Progress progress = new Progress(getFileSize(inPath) * 2, 0);
        connectSuccess();
        StringBuilder sb = new StringBuilder();

        parseForTemplates(inPath, sb, progress);
        if (sb.length() > 0) {
            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + "Templates.lua", "UTF-8")) {
                writer.println(sb);
            }
        }

        sb = new StringBuilder();
        HashSet<String> set = new HashSet<>();

        parseFiles(inPath, sb, set, progress);
        if (sb.length() > 0) {
            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                writer.println(sb);
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

    static class Progress {
        private long total;
        private long current;

        public Progress(long total, long current) {
            this.total = total;
            this.current = current;
        }

        public double getProgress() {
            return (double) current / total;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(long current) {
            this.current = current;
        }
    }
}
