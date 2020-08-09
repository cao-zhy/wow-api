package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.util.Utils;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class GlobalStringsPane extends BaseApiPane {
    private static final String FILE_NAME = "GlobalStrings.lua";
    private ChoiceBox<String> cbLanguage;

    public GlobalStringsPane(String name, EnumVersionType versionType) {
        super(name, versionType);
        cbLanguage = new ChoiceBox<>(FXCollections.observableArrayList("TW", "CN", "BR/PT", "DE", "ES", "FR", "GB",
                "IT", "KR", "MX", "RU"));
        HBox rightNode = (HBox) getRight();
        rightNode.getChildren().add(0, cbLanguage);

        cbLanguage.getSelectionModel().selectFirst();
    }

    @Override
    public void download() throws IOException {
        downloadFxmlFile(FILE_NAME, cbLanguage.getValue());
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return Utils.getRemoteBuild(FILE_NAME);
    }

    public ChoiceBox<String> getCbLanguage() {
        return cbLanguage;
    }

    public void setCbLanguage(ChoiceBox<String> cbLanguage) {
        this.cbLanguage = cbLanguage;
    }
}
