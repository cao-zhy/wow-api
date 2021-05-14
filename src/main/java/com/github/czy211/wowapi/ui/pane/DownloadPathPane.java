package com.github.czy211.wowapi.ui.pane;

import com.github.czy211.wowapi.util.PathUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DownloadPathPane extends HBox {
    private static final DownloadPathPane pane = new DownloadPathPane();
    private final TextField tfPath;
    private final Button btSelect;
    
    private DownloadPathPane() {
        super(5);
        
        Label lbName = new Label("下载位置");
        tfPath = new TextField();
        btSelect = new Button("选择文件夹");
        Button btOpen = new Button("打开文件夹");
        
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);
        getChildren().addAll(lbName, tfPath, btSelect, btOpen);
        setHgrow(tfPath, Priority.ALWAYS);
        tfPath.setFocusTraversable(false);
        tfPath.setEditable(false);
        
        String downloadPath = PathUtil.getDownloadPath();
        tfPath.setText(downloadPath);
        
        btOpen.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(new File(downloadPath));
            } catch (IOException e) {
                e.printStackTrace();
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
