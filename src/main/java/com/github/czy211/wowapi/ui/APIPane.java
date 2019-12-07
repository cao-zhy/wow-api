package com.github.czy211.wowapi.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public abstract class APIPane extends HBox {
    private Label label;
    private Label status;
    private Button check;
    private Button download;

    public APIPane() {
        setPadding(new Insets(0, 10, 10, 10));
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        label = new Label();
        status = new Label();
        check = new Button("检查更新");
        download = new Button("下载");
        getChildren().addAll(label, status, check, download);

        label.setMaxWidth(120);
        setHgrow(label, Priority.ALWAYS);
        status.setMaxWidth(Double.MAX_VALUE);
        setHgrow(status, Priority.ALWAYS);
        status.setAlignment(Pos.CENTER);
    }

    protected void updateStatus(String text) {
        Platform.runLater(() -> status.setText(text));
    }

    public abstract void checkStatus();

    public Button getCheck() {
        return check;
    }

    public Label getLabel() {
        return label;
    }

    public Label getStatus() {
        return status;
    }

    public Button getDownload() {
        return download;
    }
}
