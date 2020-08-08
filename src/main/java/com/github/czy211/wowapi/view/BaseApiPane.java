package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.util.Utils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseApiPane extends BorderPane {
    private String name;
    private Label lbName;
    private Label lbVersion;
    private Label lbStatus;
    private Hyperlink hlLink;
    private Button btDownload;
    private ProgressBar progressBar;

    public BaseApiPane(String name) {
        setPadding(new Insets(5, 10, 5, 10));

        this.name = name;
        lbName = new Label(name);
        lbVersion = new Label();
        lbStatus = new Label();
        hlLink = new Hyperlink();
        HBox centerNode = new HBox(lbVersion, lbStatus);
        btDownload = new Button("下载");
        progressBar = new ProgressBar(0);
        HBox rightNode = new HBox(5, btDownload);

        setCenter(centerNode);
        setLeft(lbName);
        setRight(rightNode);
        setBottom(progressBar);

        centerNode.setAlignment(Pos.CENTER);
        rightNode.setAlignment(Pos.CENTER);
        setAlignment(lbName, Pos.CENTER);
        setMargin(centerNode, new Insets(0, 5, 0, 5));
        setMargin(progressBar, new Insets(3, 0, 0, 0));
        progressBar.setVisible(false);
        progressBar.prefWidthProperty().bind(widthProperty());

        hlLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI(hlLink.getText()));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        AtomicReference<Thread> thread = new AtomicReference<>(new Thread(() -> {
        }));
        AtomicLong threadId = new AtomicLong();

        btDownload.setOnAction(event -> {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if (t.getId() == threadId.get() && t.getState() == Thread.State.RUNNABLE) {
                    // 中断之前的下载线程
                    t.interrupt();
                    return;
                }
            }

            Platform.runLater(() -> {
                progressBar.setVisible(true);
                lbStatus.setTextFill(Color.BLUE);
                lbStatus.setText("下载中…… 0%");
                centerNode.getChildren().remove(hlLink);
                btDownload.setText("取消下载");
                progressBar.setProgress(0);
            });

            thread.set(new Thread(() -> {
                try {
                    download();
                    if (Thread.currentThread().isInterrupted()) {
                        // 下载线程被中断
                        Platform.runLater(() -> {
                            lbStatus.setTextFill(Color.RED);
                            lbStatus.setText("已取消下载");
                            centerNode.getChildren().remove(hlLink);
                        });
                    } else {
                        // 下载线程没有被中断，则下载完成
                        Platform.runLater(() -> {
                            lbStatus.setTextFill(Color.GREEN);
                            lbStatus.setText("下载完成");
                            centerNode.getChildren().remove(hlLink);
                            updateLbVersionText();
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        lbStatus.setTextFill(Color.RED);
                        lbStatus.setText("下载失败！无法连接到");
                        centerNode.getChildren().add(hlLink);
                        hlLink.setText(e.getMessage());
                    });
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    btDownload.setText("下载");
                });
            }));
            threadId.set(thread.get().getId());
            thread.get().start();
        });
    }

    public void updateLbVersionText() {
        String downloadPath = Utils.getDownloadPath();
        File filepath = new File(downloadPath + name);
        if (filepath.exists()) {
            lbVersion.setText(getVersion());
        } else {
            lbVersion.setText("文件不存在！");
        }
    }

    public abstract String getVersion();

    public abstract void download() throws IOException;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Label getLbName() {
        return lbName;
    }

    public void setLbName(Label lbName) {
        this.lbName = lbName;
    }

    public Label getLbVersion() {
        return lbVersion;
    }

    public void setLbVersion(Label lbVersion) {
        this.lbVersion = lbVersion;
    }

    public Label getLbStatus() {
        return lbStatus;
    }

    public void setLbStatus(Label lbStatus) {
        this.lbStatus = lbStatus;
    }

    public Hyperlink getHlLink() {
        return hlLink;
    }

    public void setHlLink(Hyperlink hlLink) {
        this.hlLink = hlLink;
    }

    public Button getBtDownload() {
        return btDownload;
    }

    public void setBtDownload(Button btDownload) {
        this.btDownload = btDownload;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
