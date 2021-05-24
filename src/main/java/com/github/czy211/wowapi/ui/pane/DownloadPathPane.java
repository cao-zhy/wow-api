package com.github.czy211.wowapi.ui.pane;

import com.github.czy211.wowapi.util.PathUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadPathPane extends HBox {
    private static final DownloadPathPane pane = new DownloadPathPane();
    private final TextField tfPath;
    private final Button btSelect;
    private final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    
    private DownloadPathPane() {
        super(5);
    
        Label lbName = new Label("下载位置");
        tfPath = new TextField();
        btSelect = new Button("选择文件夹");
        Button btOpen = new Button("打开");
        Button btClear = new Button("清理");
        alert.setHeaderText(null);
    
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);
        getChildren().addAll(lbName, tfPath, btSelect, btOpen, btClear);
        setHgrow(tfPath, Priority.ALWAYS);
        tfPath.setFocusTraversable(false);
        tfPath.setEditable(false);
    
        String downloadPath = PathUtil.getDownloadPath();
        tfPath.setText(downloadPath);
    
        btOpen.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(new File(PathUtil.getDownloadPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    
        btClear.setOnAction(event -> {
            File folder = new File(PathUtil.getDownloadPath());
            File[] files = folder.listFiles();
            List<String> list = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String filename = file.getName();
                        if (filename.endsWith(".old")) {
                            if (!file.delete()) {
                                list.add(filename);
                            }
                        }
                    }
                }
                StringBuilder msg = new StringBuilder("清理完成");
                if (list.size() > 0) {
                    msg.append("\n以下文件无法删除：\n");
                    for (String filename : list) {
                        msg.append(filename).append("\n");
                    }
                }
                alert.setContentText(msg.toString());
                alert.showAndWait();
            }
        });
    }
    
    public static DownloadPathPane getInstance() {
        return pane;
    }
    
    public Button getBtSelect() {
        return btSelect;
    }
    
    public TextField getTfPath() {
        return tfPath;
    }
}
