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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseFilesPane extends BaseApiPane {
    private static final Pattern FUNCTION_PARENT_NAME = Pattern.compile("function\\s*(\\w+(\\.\\w+)*)[.:]\\w+\\s*"
            + "\\(.*\\)");
    private static final Pattern FUNCTION_NAME = Pattern.compile("_G\\.(\\w+)\\s*=\\s*(\\w+)$");
    private Label lbBicName;
    private TextField tfBicPath;
    private Button btSelect;
    private double total;
    private double current;

    public ParseFilesPane(String name, EnumVersionType versionType) {
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

    public void parseFile(File path, StringBuilder sb, HashSet<String> set) {
        File[] filePaths = path.listFiles();
        if (filePaths != null) {
            for (File inPath : filePaths) {
                if (inPath.isDirectory()) {
                    parseFile(inPath, sb, set);
                } else {
                    String filename = inPath.getName();
                    try {
                        InputStream inputStream = new FileInputStream(inPath);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                                StandardCharsets.UTF_8));
                        if (filename.endsWith(".lua")) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                String name = "";
                                Matcher matcher = FUNCTION_PARENT_NAME.matcher(line);
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
                                matcher = FUNCTION_NAME.matcher(line);
                                if (matcher.find() && matcher.group(1).equals(matcher.group(2))) {
                                    // 添加 Blizzard_CombatLog.lua 中使用 _G.xxx = xxx 方式定义的全局函数
                                    sb.append("function ").append(matcher.group(1)).append("(...) end\n\n");
                                }
                            }
                            current += getFileSize(inPath);
                            updateProgress(current / total);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public long getFileSize(File filepath) {
        if (filepath.isFile() && filepath.getName().endsWith(".lua")) {
            return filepath.length();
        } else if (filepath.isDirectory()) {
            long total = 0;
            File[] files = filepath.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".lua")) {
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
        parseFile(inPath, sb, set);
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
