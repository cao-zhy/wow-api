package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.PathConst;
import com.github.czy211.wowapi.constant.PropConst;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MainWindow extends Application {
    @Override
    public void start(Stage primaryStage) {
        VBox mainPane = new VBox(5);
        mainPane.setStyle("-fx-background-color: darkgray");
        mainPane.setPadding(new Insets(10));

        // 没有 conf 和 downloads 文件夹时创建它们
        File dir = new File(PathConst.CONFIG);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("创建 conf 文件夹成功");
            } else {
                System.out.println("创建 conf 文件夹失败");
            }
        }
        dir = new File(PathConst.DEFAULT_DOWNLOAD);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("创建 downloads 文件夹成功");
            } else {
                System.out.println("创建 downloads 文件夹失败");
            }
        }

        DownloadPathPane downloadPathPane = new DownloadPathPane();
        WidgetHierarchyPane widgetHierarchyPane = new WidgetHierarchyPane("Widget_Hierarchy.png", EnumVersionType.NONE);
        WowApiPane wowApiPane = new WowApiPane("WoW_API.lua", EnumVersionType.TIMESTAMP);
        mainPane.getChildren().addAll(downloadPathPane, widgetHierarchyPane, wowApiPane);

        for (int i = 0; i < mainPane.getChildren().size(); i++) {
            Node node = mainPane.getChildren().get(i);
            node.setStyle("-fx-background-color: white");
            VBox.setVgrow(node, Priority.ALWAYS);
        }

        DirectoryChooser chooser = new DirectoryChooser();
        downloadPathPane.getBtSelect().setOnAction(event -> {
            File filepath = chooser.showDialog(primaryStage);
            if (filepath != null) {
                String path = filepath.getPath().replaceAll("\\\\", "/");
                path = path + (path.endsWith("/") ? "" : "/");
                downloadPathPane.getTfPath().setText(path);
                // 将下载位置写入配置文件中
                File configFilePath = new File(PathConst.CONFIG_FILE);
                if (configFilePath.exists()) {
                    Properties properties = new Properties();
                    try {
                        // 不加载 properties 就写入的话会清空之前的属性
                        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                                StandardCharsets.UTF_8));
                        properties.load(reader);
                        try (PrintWriter writer = new PrintWriter(configFilePath, "UTF-8")) {
                            properties.setProperty(PropConst.DOWNLOAD_PATH, path);
                            properties.store(writer, null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 创建配置文件并写入下载位置
                    try (PrintWriter writer = new PrintWriter(configFilePath, "UTF-8")) {
                        writer.println(PropConst.DOWNLOAD_PATH + "=" + path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Scene scene = new Scene(mainPane);
        // 调整进度条高度
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        primaryStage.setOnShown(event -> {
            // 通过设置所有 BaseApiPane 的 lbName 的宽度为它们的最大宽度，使 lbName 对齐
            double maxWidth = 0;
            for (int i = 0; i < mainPane.getChildren().size(); i++) {
                Node node = mainPane.getChildren().get(i);
                if (node instanceof BaseApiPane) {
                    BaseApiPane pane = (BaseApiPane) node;
                    double lbNameWidth = pane.getLbName().getWidth();
                    if (lbNameWidth > maxWidth) {
                        maxWidth = lbNameWidth;
                    }
                }
            }
            for (int i = 0; i < mainPane.getChildren().size(); i++) {
                Node node = mainPane.getChildren().get(i);
                if (node instanceof BaseApiPane) {
                    BaseApiPane pane = (BaseApiPane) node;
                    pane.getLbName().setPrefWidth(maxWidth);
                }
            }

            // 通过设置所有 BaseApiPane 的 rightNode 的最小宽度为它们的最大宽度，使 rightNode 对齐
            maxWidth = 0;
            for (int i = 0; i < mainPane.getChildren().size(); i++) {
                Node node = mainPane.getChildren().get(i);
                if (node instanceof BaseApiPane) {
                    BaseApiPane pane = (BaseApiPane) node;
                    HBox rightNode = (HBox) pane.getRight();
                    double rightNodeWidth = rightNode.getWidth();
                    if (rightNodeWidth > maxWidth) {
                        maxWidth = rightNodeWidth;
                    }
                }
            }
            for (int i = 0; i < mainPane.getChildren().size(); i++) {
                Node node = mainPane.getChildren().get(i);
                if (node instanceof BaseApiPane) {
                    BaseApiPane pane = (BaseApiPane) node;
                    HBox rightNode = (HBox) pane.getRight();
                    rightNode.setMinWidth(maxWidth);
                }
            }

            // 设置 stage 的最小大小为首选大小
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.setMinHeight(primaryStage.getHeight());

            primaryStage.setWidth(800);
        });
        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue && newValue) {
                // 获得焦点时，更新文件版本信息
                for (int i = 0; i < mainPane.getChildren().size(); i++) {
                    Node node = mainPane.getChildren().get(i);
                    if (node instanceof BaseApiPane) {
                        BaseApiPane pane = (BaseApiPane) node;
                        pane.updateLbVersionText();
                    }
                }
            }
        });
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/wow-logo.png")));
        primaryStage.setTitle("World of Warcraft API");
        primaryStage.show();
    }
}
