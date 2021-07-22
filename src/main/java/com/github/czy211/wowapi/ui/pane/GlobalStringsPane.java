package com.github.czy211.wowapi.ui.pane;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;

public class GlobalStringsPane extends FxmlPane {
  private static final String name = "Global_Strings";
  private static final String extension = ".txt";
  private static final GlobalStringsPane pane = new GlobalStringsPane();
  private final ChoiceBox<String> choiceBox;

  private GlobalStringsPane() {
    super(name, extension);
    choiceBox = new ChoiceBox<>(FXCollections.observableArrayList("TW", "CN", "EN", "BR/PT", "DE", "ES", "FR", "GB",
        "IT", "KR", "MX", "RU"));
    getRightPane().getChildren().add(0, choiceBox);
    choiceBox.getSelectionModel().selectFirst();
  }

  public static GlobalStringsPane getInstance() {
    return pane;
  }

  @Override
  public void download() {
    String language = choiceBox.getValue();
    if ("BR/PT".equals(language)) {
      language = "BR";
    }
    downloadFile(getDownloadUrl(language), FXML_URL);
  }
}
