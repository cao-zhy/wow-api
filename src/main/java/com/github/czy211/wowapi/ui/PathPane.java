package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.i18n.I18n;
import com.github.czy211.wowapi.util.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.File;
import java.io.IOException;

public class PathPane extends HBox {
    private TextField path;
    private Button choose;

    public PathPane() {
        setPadding(new Insets(10));
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(I18n.getText("ui_label_download_directory"));
        path = new TextField();
        choose = new Button(I18n.getText("ui_button_select_directory"));
        Button open = new Button(I18n.getText("ui_button_open_directory"));
        getChildren().addAll(label, path, choose, open);
        setHgrow(path, Priority.ALWAYS);

        path.setEditable(false);
        path.setFocusTraversable(false);
        path.setText(Utils.getOutputDirectory());

        open.setOnAction(event -> {
            File file = new File(path.getText());
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("cmd.exe /c start explorer \"" + file + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public TextField getPath() {
        return path;
    }

    public Button getChoose() {
        return choose;
    }
}
