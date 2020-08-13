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
import java.util.*;

public class GlobalVariablesPane extends BaseApiPane {
    private static final String CHILD_NAME_PREFIX = "$parent";
    private static final HashMap<String, String> INTRINSIC_MIXIN = new HashMap<>();

    static {
        INTRINSIC_MIXIN.put("DropDownToggleButton", "DropDownToggleButtonMixin");
        INTRINSIC_MIXIN.put("ContainedAlertFrame", "ContainedAlertFrameMixin");
        INTRINSIC_MIXIN.put("ItemButton", "ItemButtonMixin");
        INTRINSIC_MIXIN.put("ScrollingMessageFrame", "ScrollingMessageFrameMixin");
    }

    private Label lbBicName;
    private TextField tfBicPath;
    private Button btSelect;
    /**
     * 模板名称和它的子全局变量名称映射表，全局变量名称和它对应的类型映射
     */
    private HashMap<String, HashMap<String, String>> templates = new HashMap<>();
    /**
     * 模板名称和它的父类名称映射表
     */
    private HashMap<String, ArrayList<String>> parents = new HashMap<>();

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

    public void download(File path, StringBuilder sb, HashSet<String> set, Progress progress) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    download(inPath, sb, set, progress);
                } else {
                    String filename = inPath.getName();
                    try {
                        InputStream inputStream = new FileInputStream(inPath);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                                StandardCharsets.UTF_8));
                        if (filename.endsWith(".lua")) {
                            // 遍历 lua 文件
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
                                // 因为暴雪使用了 local 定义 TickerPrototype，所以要添加它的函数
                                if ("TickerPrototype".equals(name)) {
                                    sb.append(line).append(" end\n\n");
                                }
                            }
                        } else if (filename.endsWith(".xml")) {
                            Document document = Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser());
                            Elements elements = document.select("Ui > [name]:not([virtual=true]), "
                                    + "Ui > [name]:not([virtual=true]) [name]:not([virtual=true])");
                            for (Element element : elements) {
                                String name = element.attr("name");

                                // 处理名称以 “$parent” 开头的全局变量
                                int index = -1;
                                while (name.startsWith(CHILD_NAME_PREFIX)) {
                                    for (int i = index + 1; i < element.parents().size(); i++) {
                                        Element parent = element.parents().get(i);
                                        String parentName = parent.attr("name");
                                        if (!"".equals(parentName)) {
                                            // 使用父元素的名称替换 “$parent”
                                            name = name.replace(CHILD_NAME_PREFIX, parentName);
                                            index++;
                                            break;
                                        }
                                    }
                                }

                                String tagName = element.tagName();
                                String inherits = element.attr("inherits");
                                String mixins = element.attr("mixin");
                                if ("".equals(mixins)) {
                                    mixins = element.attr("secureMixin");
                                }

                                if (!"".equals(inherits) && !"FontString".equals(tagName)) {
                                    for (String inherit : inherits.split(", |,")) {
                                        // 添加从父类继承的全局变量
                                        appendParentGlobalVars(sb, name, inherit);
                                    }
                                }

                                String intrinsics = element.attr("intrinsic");
                                if ("true".equals(intrinsics)) {
                                    sb.append("---@class ").append(name).append(":").append(tagName).append("\n");
                                } else {
                                    sb.append("---@type ").append(tagName).append(
                                            getOtherTypes(tagName, inherits, mixins)).append("\n");
                                }
                                sb.append(name).append(" = {\n");
                                appendParentKeys(sb, element, 1);
                                sb.append("}\n\n");
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

    public void initialTemplates(File path, StringBuilder sb) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    initialTemplates(inPath, sb);
                } else {
                    String filename = inPath.getName();
                    if (filename.endsWith(".xml")) {
                        try {
                            InputStream in = new FileInputStream(inPath);
                            Document document = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
                            // Ui 标签下的非字体类型的一级元素 virtual 属性值是 true 的是模板类型
                            Elements elements = document.select("Ui > [virtual=true]:not(Font,FontString,FontFamily)");
                            for (Element element : elements) {
                                String name = element.attr("name");
                                if (!name.contains("-")) {
                                    String inherits = element.attr("inherits");
                                    if (!"".equals(inherits)) {
                                        // 把模板的父类添加进 parents
                                        parents.put(name, new ArrayList<>(Arrays.asList(inherits.split(", |,"))));
                                    }
                                    // 添加模板的子全局变量名称后缀
                                    templates.put(name, new HashMap<>());
                                    addGlobalVarSuffixNames(element, name);
                                    sb.append("---@class ").append(name).append("\n").append(name).append(" = {\n");
                                    // 添加模板的字段
                                    appendParentKeys(sb, element, 1);
                                    sb.append("}\n\n");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void appendParentKeys(StringBuilder sb, Element element, int index) {
        Elements elements = element.select("* > [parentKey]");
        for (Element e : elements) {
            String parentKey = e.attr("parentKey");
            if (!parentKey.contains("-")) {
                String tagName = e.tagName();
                tagName = replaceTagName(tagName);
                String inherits = e.attr("inherits");
                String mixins = e.attr("mixin");
                if ("".equals(mixins)) {
                    mixins = e.attr("secureMixin");
                }

                for (int i = 0; i < index; i++) {
                    sb.append("    ");
                }
                sb.append("---@type ").append(tagName).append(getOtherTypes(tagName, inherits, mixins)).append("\n");
                for (int i = 0; i < index; i++) {
                    sb.append("    ");
                }
                sb.append(parentKey).append(" = {\n");
                appendParentKeys(sb, e, index + 1);
                for (int i = 0; i < index; i++) {
                    sb.append("    ");
                }
                sb.append("},\n");
            }
        }
    }

    public void addGlobalVarSuffixNames(Element element, String templateName) {
        String name = element.attr("name");
        Elements elements = element.select("* > [name^=" + CHILD_NAME_PREFIX + "]");
        for (Element e : elements) {
            HashMap<String, String> map = templates.get(templateName);
            String childName = e.attr("name");
            if (!name.equals(templateName)) {
                childName = childName.replace(CHILD_NAME_PREFIX, name);
            }
            String tagName = e.tagName();
            tagName = replaceTagName(tagName);
            map.put(childName.substring(CHILD_NAME_PREFIX.length()), tagName);
            addGlobalVarSuffixNames(e, templateName);
        }
    }

    public void appendInherits(StringBuilder sb, String name) {
        ArrayList<String> parentNames = parents.get(name);
        if (parentNames != null) {
            for (String parentName : parentNames) {
                sb.append("|").append(parentName);
                appendInherits(sb, parentName);
            }
        }
    }

    public void appendParentGlobalVars(StringBuilder sb, String name, String inherit) {
        HashMap<String, String> map = templates.get(inherit);
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                // 名称是当前 widget 的名称加上子名称后缀
                sb.append("---@type ").append(entry.getValue()).append("\n").append(name).append(entry.getKey())
                        .append(" = {}\n\n");
            }
        }
        ArrayList<String> parentNames = parents.get(inherit);
        if (parentNames != null) {
            for (String parentName : parentNames) {
                appendParentGlobalVars(sb, name, parentName);
            }
        }
    }

    public String replaceTagName(String tagName) {
        if (tagName.endsWith("Texture")) {
            tagName = "Texture";
        } else if ("ButtonText".equals(tagName)) {
            tagName = "FontString";
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

    public StringBuilder getOtherTypes(String tagName, String inherits, String mixins) {
        StringBuilder sb = new StringBuilder();
        String intrinsicMixin = INTRINSIC_MIXIN.get(tagName);
        if (intrinsicMixin != null) {
            sb.append("|").append(intrinsicMixin);
        }
        if (!"".equals(inherits) && !"FontString".equals(tagName)) {
            String[] inheritList = inherits.split(", |,");
            for (String inherit : inheritList) {
                if (!inherit.contains("-")) {
                    sb.append("|").append(inherit);
                    appendInherits(sb, inherit);
                }
            }
        }
        if (!"".equals(mixins)) {
            String[] mixinList = mixins.split(", |,");
            for (String mixin : mixinList) {
                sb.append("|").append(mixin);
            }
        }
        return sb;
    }

    @Override
    public void download() throws IOException {
        File inPath = new File(Utils.getBicPath());
        StringBuilder sb = new StringBuilder();
        HashSet<String> set = new HashSet<>();

        initialTemplates(inPath, sb);
        connectSuccess();
        download(inPath, sb, set, new Progress(getFileSize(inPath), 0));
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

    public HashMap<String, HashMap<String, String>> getTemplates() {
        return templates;
    }

    public void setTemplates(HashMap<String, HashMap<String, String>> templates) {
        this.templates = templates;
    }

    public HashMap<String, ArrayList<String>> getParents() {
        return parents;
    }

    public void setParents(HashMap<String, ArrayList<String>> parents) {
        this.parents = parents;
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
