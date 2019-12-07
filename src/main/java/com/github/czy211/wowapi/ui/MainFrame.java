package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.model.FXMLPage;
import com.github.czy211.wowapi.model.WikiPage;
import com.github.czy211.wowapi.util.Utils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MainFrame extends Application {
    private static final int MIN_WIDTH = 710;
    private static final int MIN_HEIGHT = 210;

    @Override
    public void start(Stage primaryStage) {
        VBox mainPane = new VBox();
        PathPane pathPane = new PathPane();

        WikiPage WoWAPI = new WikiPage("/World_of_Warcraft_API", "WoW API", "WoWAPI.lua", "AttemptToSaveBindings");
        WikiPage luaAPI = new WikiPage("/Lua_functions", "Lua API", "LuaAPI.lua");
        WikiPage widgetAPI = new WikiPage("/Widget_API", "Widget API", "WidgetAPI.lua");
        WikiPage widgetHandler = new WikiPage("/Widget_handlers", "Widget Handler", "WidgetHandler.lua");
        FXMLPage frameXMLPage = new FXMLPage();

        mainPane.getChildren().addAll(pathPane, new WikiAPIPane(WoWAPI), new WikiAPIPane(luaAPI),
                new WikiAPIPane(widgetAPI), new WikiAPIPane(widgetHandler), new FXMLAPIPane(frameXMLPage));

        DirectoryChooser chooser = new DirectoryChooser();
        pathPane.getChoose().setOnAction(event -> {
            File file = chooser.showDialog(primaryStage);
            if (file != null) {
                String outputPath = file.getPath().replaceAll("\\\\", "/");
                pathPane.getPath().setText(outputPath);
                try (PrintWriter output = new PrintWriter(Utils.getPath() + "config.properties")) {
                    output.println("outputPath=" + outputPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (Node pane : mainPane.getChildren()) {
                    if (pane instanceof APIPane) {
                        ((APIPane) pane).checkStatus();
                    }
                }
            }
        });

        Scene scene = new Scene(mainPane, MIN_WIDTH, MIN_HEIGHT);
        primaryStage.showingProperty().addListener((observable, oldValue, showing) -> {
            if (showing) {
                primaryStage.setMinWidth(primaryStage.getWidth());
                primaryStage.setMinHeight(primaryStage.getHeight());
            }
        });
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/wow-logo.png")));
        primaryStage.setTitle("World of Warcraft API");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
