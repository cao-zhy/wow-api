package com.github.czy211.wowapi.ui.pane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;

public abstract class FxmlPane extends BasePane {
  private static final String baseUri = "https://www.townlong-yak.com";
  public static final String FXML_URL = baseUri + "/framexml/live";

  public FxmlPane(String name, String extension) {
    super(name, extension, FXML_URL);
  }

  @Override
  public String getRemoteVersion() throws IOException {
    String text = getFileTr().select("td").get(1).text();
    return text.substring(text.length() - 5);
  }

  public String getDownloadUrl(String language) {
    String downloadUrl = null;
    try {
      Element tr = getFileTr();
      Element link = language == null || "EN".equals(language) ? tr.selectFirst("a")
          : tr.selectFirst("a:contains(" + language + ")");
      downloadUrl = baseUri + link.attr("href") + "/get";
    } catch (IOException e) {
      connectFail(FXML_URL);
      e.printStackTrace();
    }
    return downloadUrl;
  }

  public String getDownloadUrl() {
    return getDownloadUrl(null);
  }

  private Element getFileTr() throws IOException {
    return Jsoup.connect(FXML_URL).get().selectFirst("tr:contains(" + getName().replaceAll("_", "") + ")");
  }
}
