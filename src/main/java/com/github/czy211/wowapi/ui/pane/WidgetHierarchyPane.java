package com.github.czy211.wowapi.ui.pane;

import com.github.czy211.wowapi.util.PathUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class WidgetHierarchyPane extends WikiPane {
  private static final String name = "Widget_Hierarchy";
  private static final String extension = ".png";
  private static final String url = BASE_URI + "/wiki/File:Widget_Hierarchy.png";
  private static final WidgetHierarchyPane pane = new WidgetHierarchyPane();

  private WidgetHierarchyPane() {
    super(name, extension, url);
  }

  public static WidgetHierarchyPane getInstance() {
    return pane;
  }

  @Override
  public void download() {
    String downloadUrl = "";
    try {
      Element element = Jsoup.connect(url).get().selectFirst(".filehistory-selected").selectFirst("a");
      downloadUrl = element.attr("href");
    } catch (IOException e) {
      connectFail(url);
      e.printStackTrace();
    }
    try {
      HttpsURLConnection connection = (HttpsURLConnection) new URL(downloadUrl).openConnection();
      connected(connection.getContentLengthLong());
      String filename = name + "-" + getRemoteVersion() + extension;
      String filepath = PathUtil.getDownloadPath() + filename;
      try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
           BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filepath))) {
        byte[] temp = new byte[8192];
        int length;
        while ((length = in.read(temp)) != -1) {
          if (Thread.currentThread().isInterrupted()) {
            canceled();
            return;
          }
          out.write(temp, 0, length);
          out.flush();
          increaseCurrent(length);
        }
      }
      downloadComplete(filename);
    } catch (IOException e) {
      connectFail(downloadUrl);
      e.printStackTrace();
    }
  }

  @Override
  public String getRemoteVersion() throws IOException {
    String dateTime = Jsoup.connect(url).get().selectFirst(".filehistory-selected").selectFirst("a").text();
    return formatDateTime(dateTime, "HH:mm, d MMMM yyyy");
  }
}
