package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.i18n.I18n;
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

    public APIPane() {
        setPadding(new Insets(0, 10, 10, 10));
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        label = new Label();
        status = new Label();
        Button check = new Button(I18n.getText("ui_button_check_for_update"));
        Button download = new Button(I18n.getText("ui_button_download"));
        getChildren().addAll(label, status, check, download);

        label.setMaxWidth(120);
        setHgrow(label, Priority.ALWAYS);
        status.setMaxWidth(Double.MAX_VALUE);
        setHgrow(status, Priority.ALWAYS);
        status.setAlignment(Pos.CENTER);

        check.setOnAction(event -> new Thread(() -> {
            updateStatus(I18n.getText("status_checking_for_update"));
            checkForUpdate();
        }).start());

        download.setOnAction(event -> new Thread(() -> {
            updateStatus(I18n.getText("status_downloading"));
            download();
        }).start());
    }

    protected void updateStatus(String text) {
        Platform.runLater(() -> status.setText(text));
    }

    public abstract void checkStatus();

    public abstract void checkForUpdate();

    public abstract void download();

    public Label getLabel() {
        return label;
    }

    public Label getStatus() {
        return status;
    }
}
