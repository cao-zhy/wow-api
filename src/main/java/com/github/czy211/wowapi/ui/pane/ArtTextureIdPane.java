package com.github.czy211.wowapi.ui.pane;

public class ArtTextureIdPane extends FxmlPane {
  private static final String name = "Art_Texture_ID";
  private static final String extension = ".txt";
  private static final ArtTextureIdPane pane = new ArtTextureIdPane();

  private ArtTextureIdPane() {
    super(name, extension);
  }

  public static ArtTextureIdPane getInstance() {
    return pane;
  }

  @Override
  public void download() {
    downloadFile(getDownloadUrl(), FXML_URL);
  }
}
