package com.github.czy211.wowapi.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow extends Application {
    @Override
    public void start(Stage primaryStage) {
        VBox mainPane = new VBox(5);
        mainPane.setStyle("-fx-background-color: darkgray");
        mainPane.setPadding(new Insets(10));

        Scene scene = new Scene(mainPane);

        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/wow-logo.png")));
        primaryStage.setTitle("World of Warcraft API");
        primaryStage.show();
    }
}
