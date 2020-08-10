package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.PathConst;
import com.github.czy211.wowapi.constant.PropConst;
import com.github.czy211.wowapi.util.Utils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

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
        WidgetApiPane widgetApiPane = new WidgetApiPane("Widget_API.lua", EnumVersionType.TIMESTAMP);
        WidgetScriptTypesPane widgetScriptTypesPane = new WidgetScriptTypesPane("Widget_Script_Types.lua",
                EnumVersionType.TIMESTAMP);
        WidgetScriptHandlersPane widgetScriptHandlersPane = new WidgetScriptHandlersPane("Widget_Script_Handlers.lua",
                EnumVersionType.TIMESTAMP);
        LuaApiPane luaApiPane = new LuaApiPane("Lua_API.lua", EnumVersionType.TIMESTAMP);
        GlobalStringsPane globalStringsPane = new GlobalStringsPane("Global_Strings.lua", EnumVersionType.BUILD);
        ArtTextureIdPane artTextureIdPane = new ArtTextureIdPane("Art_Texture_Id.lua", EnumVersionType.BUILD);
        AtlasInfoPane atlasInfoPane = new AtlasInfoPane("Atlas_Info.lua", EnumVersionType.BUILD);
        SystemApiPane systemApiPane = new SystemApiPane("System_API.lua", EnumVersionType.BUILD);
        GlobalVariablesPane globalVariablesPane = new GlobalVariablesPane("Global_Variables.lua", EnumVersionType.NONE);

        mainPane.getChildren().addAll(downloadPathPane, widgetHierarchyPane, wowApiPane, widgetApiPane,
                widgetScriptTypesPane, widgetScriptHandlersPane, luaApiPane, globalStringsPane, artTextureIdPane,
                atlasInfoPane, systemApiPane, globalVariablesPane);

        for (int i = 0; i < mainPane.getChildren().size(); i++) {
            Node node = mainPane.getChildren().get(i);
            node.setStyle("-fx-background-color: white");
            VBox.setVgrow(node, Priority.ALWAYS);
        }

        DirectoryChooser chooser = new DirectoryChooser();
        downloadPathPane.getBtSelect().setOnAction(event -> {
            File filepath = chooser.showDialog(primaryStage);
            btSelectOnClick(filepath, downloadPathPane.getTfPath(), PropConst.DOWNLOAD_PATH);
            if (filepath != null) {
                refreshPane(mainPane);
            }
        });
        globalVariablesPane.getBtSelect().setOnAction(event -> {
            File filepath = chooser.showDialog(primaryStage);
            btSelectOnClick(filepath, globalVariablesPane.getTfBicPath(), PropConst.BIC_PATH);
            if (filepath != null) {
                globalVariablesPane.getBtDownload().setDisable(false);
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
                // 获得焦点时，刷新所有面板
                refreshPane(mainPane);
            }
        });
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/wow-logo.png")));
        primaryStage.setTitle("World of Warcraft API");
        primaryStage.show();
    }

    public void refreshPane(Pane pane) {
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Node node = pane.getChildren().get(i);
            if (node instanceof BaseApiPane) {
                BaseApiPane apiPane = (BaseApiPane) node;
                apiPane.updateLbVersionText();
            }
        }
    }

    public void btSelectOnClick(File filepath, TextField textField, String property) {
        if (filepath != null) {
            String path = filepath.getPath().replaceAll("\\\\", "/");
            path = path + (path.endsWith("/") ? "" : "/");
            // 设置 textField 的值
            textField.setText(path);
            // 将属性写入配置文件
            Utils.setConfigProperty(property, path);
        }
    }
}
