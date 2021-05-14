package com.github.czy211.wowapi.ui.pane;

public class AtlasInfoPane extends FxmlPane {
    private static final String name = "Atlas_Info";
    private static final String extension = ".txt";
    private static final AtlasInfoPane pane = new AtlasInfoPane();
    
    private AtlasInfoPane() {
        super(name, extension);
    }
    
    public static AtlasInfoPane getInstance() {
        return pane;
    }
    
    @Override
    public void download() {
        downloadFile(getDownloadUrl(), FXML_URL);
    }
}
