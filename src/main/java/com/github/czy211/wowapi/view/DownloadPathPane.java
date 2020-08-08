package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.PathConst;
import com.github.czy211.wowapi.constant.PropConst;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class DownloadPathPane extends HBox {
    private Label lbName;
    private TextField tfPath;
    private Button btSelect;
    private Button btOpen;

    public DownloadPathPane() {
        super(5);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        lbName = new Label("下载位置");
        tfPath = new TextField();
        btSelect = new Button("选择文件夹");
        btOpen = new Button("打开文件夹");

        getChildren().addAll(lbName, tfPath, btSelect, btOpen);

        setHgrow(tfPath, Priority.ALWAYS);
        tfPath.setFocusTraversable(false);
        tfPath.setEditable(false);
        File configPath = new File(PathConst.CONFIG_FILE);
        if (configPath.exists() && configPath.isFile()) {
            // 配置文件存在，读取下载位置
            Properties properties = new Properties();
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configPath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                String downloadPath = properties.getProperty(PropConst.DOWNLOAD_PATH);
                if (downloadPath == null) {
                    // 没有配置下载位置，使用默认下载位置
                    tfPath.setText(PathConst.DEFAULT_DOWNLOAD);
                } else {
                    tfPath.setText(downloadPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 配置文件不存在，使用默认下载位置
            tfPath.setText(PathConst.DEFAULT_DOWNLOAD);
        }

        btOpen.setOnAction(event -> {
            File downloadPath = new File(tfPath.getText());
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("cmd.exe /c start explorer \"" + downloadPath + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Label getLbName() {
        return lbName;
    }

    public void setLbName(Label lbName) {
        this.lbName = lbName;
    }

    public TextField getTfPath() {
        return tfPath;
    }

    public void setTfPath(TextField tfPath) {
        this.tfPath = tfPath;
    }

    public Button getBtSelect() {
        return btSelect;
    }

    public void setBtSelect(Button btSelect) {
        this.btSelect = btSelect;
    }

    public Button getBtOpen() {
        return btOpen;
    }

    public void setBtOpen(Button btOpen) {
        this.btOpen = btOpen;
    }
}
