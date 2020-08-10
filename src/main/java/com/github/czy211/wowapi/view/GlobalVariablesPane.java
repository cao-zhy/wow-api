package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.WidgetConst;
import com.github.czy211.wowapi.util.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class GlobalVariablesPane extends BaseApiPane {
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

    public void download(File path, StringBuilder sb, HashSet<String> set, Progress progress) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    download(inPath, sb, set, progress);
                } else {
                    String filename = inPath.getName();
                    if (filename.endsWith(".xml") || filename.endsWith(".lua")) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                                new FileInputStream(inPath), StandardCharsets.UTF_8))) {
                            String tagLine = "";
                            String line;
                            while ((line = reader.readLine()) != null) {
                                tagLine = tagLine + line.trim();
                                if (filename.endsWith(".xml")) {
                                    // 遍历 xml 文件
                                    if (!tagLine.endsWith(">")) {
                                        // 不是以 “>” 结尾，则标签没有结束，直接读取下一行
                                        continue;
                                    }
                                    // 去掉 “=” 两边的空白字符
                                    tagLine = tagLine.replaceAll("\\s*=\\s*", "=");
                                    String name = getWidgetName(tagLine);
                                    if (name != null && !name.matches(".*[-$].*")
                                            && !tagLine.contains("virtual=\"true\"")) {
                                        String tagName = getTagName(tagLine);
                                        if (!WidgetConst.EXCLUDE_TAGS.contains(tagName)) {
                                            if (tagLine.contains("intrinsic=\"true\"")) {
                                                sb.append("---@class ").append(name).append(":").append(tagName)
                                                        .append("\n");
                                            } else {
                                                sb.append("---@type ").append(tagName).append(getOtherTypes(tagLine))
                                                        .append("\n");
                                            }
                                            sb.append(name).append(" = {}").append("\n\n");
                                        }
                                    }
                                } else {
                                    // 遍历 lua 文件
                                    String name = "";
                                    if (tagLine.matches("function .+[.:].+\\(.*\\)")) {
                                        name = getFunctionParentName(tagLine);
                                    } else if (tagLine.matches("\\w+ = CreateFromMixins\\(.*\\);")) {
                                        name = tagLine.substring(0, tagLine.indexOf("=") - 1);
                                    } else if (tagLine.matches("_G\\..+=.+")) {
                                        // 添加 Blizzard_CombatLog.lua 中使用 _G.xxx = xxx 方式定义的全局函数
                                        int index = tagLine.indexOf("=");
                                        String leftName = tagLine.substring(3, index - 1);
                                        String rightName = tagLine.substring(index + 2);
                                        if (leftName.equals(rightName)) {
                                            sb.append("function ").append(leftName).append("(...) end\n\n");
                                        }
                                    }
                                    if (!"".equals(name) && !set.contains(name)) {
                                        set.add(name);
                                        sb.append("---@class ").append(name).append("\n").append(name)
                                                .append(" = {}\n\n");
                                    }
                                    // 因为暴雪使用了 local 定义 TickerPrototype，所以需要添加函数才会有提示
                                    if ("TickerPrototype".equals(name)) {
                                        sb.append(tagLine).append(" end\n\n");
                                    }
                                }
                                tagLine = "";
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

    public String getOtherTypes(String line) {
        StringBuilder sb = new StringBuilder();
        String tagName = getTagName(line);
        String intrinsicMixin = WidgetConst.INTRINSIC_MIXIN.get(tagName);
        if (intrinsicMixin != null) {
            sb.append("|").append(intrinsicMixin);
        }
        String mixins = getMixins(line);
        if (mixins != null) {
            String[] mixinList = mixins.split(",|, ");
            for (String mixin : mixinList) {
                sb.append("|").append(mixin);
            }
        }
        return sb.toString();
    }

    public String getMixins(String line) {
        int index = line.indexOf("mixin=\"");
        int offset = 7;
        if (index == -1) {
            index = line.indexOf("secureMixin=\"");
            offset = 13;
        }
        if (index == -1) {
            return null;
        }
        int startIndex = index + offset;
        int endIndex = line.indexOf("\"", startIndex);
        return line.substring(startIndex, endIndex);
    }

    public String getWidgetName(String line) {
        int index = line.indexOf("name=\"");
        if (index == -1) {
            return null;
        }
        int startIndex = index + 6;
        int endIndex = line.indexOf("\"", startIndex);
        return line.substring(startIndex, endIndex);
    }

    public String getTagName(String line) {
        int startIndex = line.indexOf("<") + 1;
        int endIndex = line.indexOf(" ", startIndex);
        return line.substring(startIndex, endIndex);
    }

    @Override
    public void download() throws IOException {
        File inPath = new File(Utils.getBicPath());
        StringBuilder sb = new StringBuilder();
        HashSet<String> set = new HashSet<>();
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
