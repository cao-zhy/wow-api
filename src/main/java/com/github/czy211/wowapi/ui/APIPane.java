package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.i18n.I18n;
import com.github.czy211.wowapi.model.APIPage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public abstract class APIPane extends HBox {
    private Label status;

    public APIPane(APIPage page) {
        setPadding(new Insets(0, 10, 10, 10));
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(page.getName());
        status = new Label();
        Button check = new Button(I18n.getText("ui_button_check_for_update"));
        Button download = new Button(I18n.getText("ui_button_download"));
        getChildren().addAll(label, status, check, download);

        // 使所有 label 具有相同的宽度
        label.setMaxWidth(120);
        setHgrow(label, Priority.ALWAYS);
        // 使 status 具有最大宽度
        status.setMaxWidth(Double.MAX_VALUE);
        setHgrow(status, Priority.ALWAYS);
        status.setAlignment(Pos.CENTER);

        // 启动一个新的线程来检查更新
        check.setOnAction(event -> new Thread(() -> {
            updateStatus(I18n.getText("status_checking_for_update"));
            checkForUpdate();
        }).start());

        // 启动一个新的线程来下载
        download.setOnAction(event -> new Thread(() -> {
            updateStatus(I18n.getText("status_downloading"));
            download();
        }).start());
    }

    /**
     * 使用 FX application 线程更新状态标签内容
     *
     * @param text 状态标签内容
     */
    protected void updateStatus(String text) {
        Platform.runLater(() -> status.setText(text));
    }

    /**
     * 设置状态内容
     */
    public abstract void setStatusText();

    /**
     * 检查更新
     */
    public abstract void checkForUpdate();

    /**
     * 下载
     */
    public abstract void download();
}
