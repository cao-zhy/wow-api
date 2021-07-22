package com.github.czy211.wowapi.ui.pane;

import com.github.czy211.wowapi.util.PathUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BasePane extends BorderPane {
  private final String name;
  private final String extension;
  private final String fileRegex;
  private final String url;
  private final Label lbName;
  private final Label lbVersion;
  private final Label lbStatus;
  private final Hyperlink hyperlink;
  private final Button btDownload;
  private final Button btCheck;
  private final ProgressBar bar;
  private final HBox center;
  private final HBox right;
  private double total;
  private long current;
  private boolean downloading;
  private boolean checking;
  private Future<?> future;
  private File oldFile;

  public BasePane(String name, String extension, String url) {
    this.name = name;
    this.extension = extension;
    this.url = url;
    fileRegex = name + "-(\\d{5,}+)" + extension;
    lbName = new Label(name + extension);
    lbVersion = new Label();
    lbStatus = new Label();
    hyperlink = new Hyperlink();
    btDownload = new Button("下载");
    btCheck = new Button("检查更新");
    bar = new ProgressBar(0);

    bar.setVisible(false);
    bar.prefWidthProperty().bind(widthProperty());

    center = new HBox(lbVersion, lbStatus);
    right = new HBox(5, btDownload, btCheck);

    setCenter(center);
    setLeft(lbName);
    setRight(right);
    setBottom(bar);

    center.setAlignment(Pos.CENTER);
    right.setAlignment(Pos.CENTER_RIGHT);
    setAlignment(lbName, Pos.CENTER);
    setMargin(center, new Insets(0, 5, 0, 5));
    setMargin(bar, new Insets(3, 0, 0, 0));
    setPadding(new Insets(5, 10, 2, 10));

    hyperlink.setOnAction(event -> {
      try {
        Desktop.getDesktop().browse(new URI(hyperlink.getText()));
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    });
  }

  public abstract void download();

  public abstract String getRemoteVersion() throws IOException;

  public void createFile(StringBuilder sb) {
    try {
      String filename = name + "-" + getRemoteVersion() + extension;
      String filepath = PathUtil.getDownloadPath() + filename;
      try (PrintWriter writer = new PrintWriter(filepath, "UTF-8")) {
        writer.print(sb);
      }
      downloadComplete(filename);
    } catch (IOException e) {
      connectFail(url);
      e.printStackTrace();
    }
  }

  public void increaseCurrent(long value) {
    current += value;
    updateProgress();
  }

  public void connected(double total) {
    current = 0;
    this.total = total;
    Platform.runLater(() -> {
      lbStatus.setText("下载中…… 0%");
      btDownload.setDisable(false);
      btDownload.setText("取消下载");
    });
  }

  public void connectFail(String url) {
    downloading = false;
    checking = false;
    Platform.runLater(() -> {
      bar.setVisible(false);
      lbStatus.setTextFill(Color.RED);
      lbStatus.setText("无法连接到 ");
      center.getChildren().add(hyperlink);
      hyperlink.setText(url);
      btDownload.setDisable(false);
      updateVersionLabel();
    });
  }

  public void downloadFile(String downloadUrl, String referer) {
    StringBuilder sb = new StringBuilder();
    try {
      HttpsURLConnection conn = (HttpsURLConnection) new URL(downloadUrl).openConnection();
      if (referer != null) {
        conn.setRequestProperty("Referer", referer);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, lik"
            + "e Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.56");
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      connected(conn.getContentLengthLong());
      String line;
      while ((line = reader.readLine()) != null) {
        if (Thread.currentThread().isInterrupted()) {
          canceled();
          return;
        }
        sb.append(line).append("\n");
        increaseCurrent(line.length());
      }
      String filename = name + "-" + getRemoteVersion() + extension;
      String filepath = PathUtil.getDownloadPath() + filename;
      try (PrintWriter writer = new PrintWriter(filepath, "UTF-8")) {
        writer.print(sb);
        downloadComplete(filename);
      }
    } catch (IOException e) {
      connectFail(downloadUrl);
      e.printStackTrace();
    }
  }

  public void downloadFile(String downloadUrl) {
    downloadFile(downloadUrl, null);
  }

  public void downloadComplete(String filename) {
    downloading = false;
    Platform.runLater(() -> {
      lbStatus.setTextFill(Color.GREEN);
      lbStatus.setText("下载完成");
      btDownload.setDisable(false);
      btDownload.setText("下载");
      bar.setVisible(false);
      updateVersionLabel();
    });
    if (oldFile != null && !oldFile.getName().equals(filename)) {
      if (!oldFile.renameTo(new File(oldFile.getAbsolutePath().replace(extension, ".old")))) {
        Platform.runLater(() -> {
          lbStatus.setTextFill(Color.RED);
          lbStatus.setText("旧文件重命名失败");
        });
      }
    }
  }

  public void updateVersionLabel() {
    String text = getFileVersion();
    Platform.runLater(() -> {
      lbVersion.setText(text == null ? "文件不存在！" : ("本地版本：" + text));
      btCheck.setDisable(text == null);
    });
  }

  public void clickDownload() {
    downloading = true;
    Platform.runLater(() -> {
      bar.setVisible(true);
      lbStatus.setTextFill(Color.BLUE);
      lbStatus.setText("正在连接……");
      center.getChildren().remove(hyperlink);
      btDownload.setDisable(true);
      btCheck.setDisable(true);
      bar.setProgress(0);
    });
  }

  public void clickCancel() {
    Platform.runLater(() -> {
      lbStatus.setTextFill(Color.RED);
      lbStatus.setText("正在取消下载");
      btDownload.setDisable(true);
    });
  }

  public void canceled() {
    downloading = false;
    Platform.runLater(() -> {
      bar.setVisible(false);
      lbStatus.setText("已取消下载");
      btDownload.setText("下载");
      btDownload.setDisable(false);
      updateVersionLabel();
    });
  }

  public void clickCheck() {
    checking = true;
    Platform.runLater(() -> {
      lbStatus.setTextFill(Color.BLUE);
      lbStatus.setText("检查更新中……");
      center.getChildren().remove(hyperlink);
      btDownload.setDisable(true);
      btCheck.setDisable(true);
      bar.setVisible(true);
      bar.setProgress(-1);
    });
  }

  public void taskCheck() {
    String localVersion = getFileVersion();
    try {
      String remoteVersion = getRemoteVersion();
      Platform.runLater(() -> {
        if (localVersion.equals(remoteVersion)) {
          lbStatus.setTextFill(Color.GREEN);
          lbStatus.setText("已是最新版本");
        } else {
          lbStatus.setTextFill(Color.RED);
          lbStatus.setText("有新版本可下载");
        }
        btDownload.setDisable(false);
        btCheck.setDisable(false);
        bar.setVisible(false);
        checking = false;
      });
    } catch (IOException e) {
      connectFail(url);
      e.printStackTrace();
    }
  }


  public String getFileVersion() {
    File[] files = new File(PathUtil.getDownloadPath()).listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          String filename = file.getName();
          Matcher m = Pattern.compile(fileRegex).matcher(filename);
          if (m.find()) {
            oldFile = file;
            return m.group(1);
          }
        }
      }
    }
    return null;
  }

  public Button getBtDownload() {
    return btDownload;
  }

  public Button getBtCheck() {
    return btCheck;
  }

  public Label getLbName() {
    return lbName;
  }

  public HBox getRightPane() {
    return right;
  }

  public String getFileRegex() {
    return fileRegex;
  }

  public long getCurrent() {
    return current;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public boolean isDownloading() {
    return downloading;
  }

  public boolean isChecking() {
    return checking;
  }

  public Future<?> getFuture() {
    return future;
  }

  public void setFuture(Future<?> future) {
    this.future = future;
  }

  private void updateProgress() {
    double progress = current / total;
    String text = String.format("%s %.1f%%", "下载中……", progress * 100);
    Platform.runLater(() -> {
      bar.setProgress(progress);
      lbStatus.setText(text);
    });
  }
}
