package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.ui.pane.*;
import com.github.czy211.wowapi.util.PathUtil;
import com.github.czy211.wowapi.util.PropUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Window extends Application {
  private final ExecutorService exec = Executors.newCachedThreadPool();
  private VBox mainPane;

  @Override
  public void start(Stage primaryStage) {
    mainPane = new VBox(5);
    mainPane.setStyle("-fx-background-color: darkgray");
    mainPane.setPadding(new Insets(10));

    createFolders();

    DownloadPathPane downloadPathPane = DownloadPathPane.getInstance();
    GlobalFramesPane framesPane = GlobalFramesPane.getInstance();

    mainPane.getChildren().addAll(
        downloadPathPane,
        GlobalFunctionsPane.getInstance(),
        WidgetFunctionsPane.getInstance(),
        LuaFunctionsPane.getInstance(),
        GlobalNumbers.getInstance(),
        framesPane,
        WidgetHierarchyPane.getInstance(),
        ScriptHandlersPane.getInstance(),
        GlobalStringsPane.getInstance(),
        ArtTextureIdPane.getInstance(),
        AtlasInfoPane.getInstance());

    DirectoryChooser dc = new DirectoryChooser();
    downloadPathPane.getBtSelect().setOnAction(event -> {
      File file = dc.showDialog(primaryStage);
      clickBtSelect(file, downloadPathPane.getTfPath(), PropUtil.DOWNLOAD_PATH);
      if (file != null) {
        updateVersionLabels();
      }
    });
    framesPane.getBtSelect().setOnAction(event -> {
      File file = dc.showDialog(primaryStage);
      clickBtSelect(file, framesPane.getTfCode(), PropUtil.CODE_PATH);
      if (file != null) {
        framesPane.getBtDownload().setDisable(false);
      }
    });

    Scene scene = new Scene(mainPane);
    // 调整进度条高度
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/progress-bar.css"))
        .toExternalForm());

    ArrayList<Region> lbNames = new ArrayList<>();
    ArrayList<Region> rights = new ArrayList<>();
    for (Node node : mainPane.getChildren()) {
      node.setStyle("-fx-background-color: white");
      VBox.setVgrow(node, Priority.ALWAYS);
      if (node instanceof BasePane) {
        BasePane pane = (BasePane) node;
        lbNames.add(pane.getLbName());
        rights.add(pane.getRightPane());

        pane.getBtDownload().setOnAction(event -> {
          Future<?> future = pane.getFuture();
          if (pane.isDownloading()) {
            if (future != null && !future.isDone()) {
              future.cancel(true);
              pane.clickCancel();
            }
          } else {
            pane.clickDownload();
            pane.setFuture(exec.submit(pane::download));
          }
        });

        pane.getBtCheck().setOnAction(event -> {
          pane.clickCheck();
          exec.execute(pane::taskCheck);
        });
      }
    }

    primaryStage.setOnShown(event -> {
      for (Region region : lbNames) {
        region.setPrefWidth(getMaxRegionWidth(lbNames));
      }
      for (Region region : rights) {
        region.setMinWidth(getMaxRegionWidth(rights));
      }
      primaryStage.setMinWidth(primaryStage.getWidth());
      primaryStage.setMinHeight(primaryStage.getHeight());
      primaryStage.setWidth(1000);
    });
    // 主窗口获得焦点时更新所有版本标签
    primaryStage.focusedProperty().addListener(((observable, oldValue, newValue) -> {
      if (!oldValue && newValue) {
        updateVersionLabels();
      }
    }));
    primaryStage.setOnCloseRequest(event -> exec.shutdownNow());
    primaryStage.setScene(scene);
    primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/wow-logo.png"))));
    primaryStage.setTitle("World of Warcraft API");
    primaryStage.show();
    primaryStage.centerOnScreen();
  }

  private void updateVersionLabels() {
    for (Node node : mainPane.getChildren()) {
      if (node instanceof BasePane) {
        BasePane pane = (BasePane) node;
        if (!pane.isDownloading() && !pane.isChecking()) {
          pane.updateVersionLabel();
        }
      }
    }
  }

  private void clickBtSelect(File file, TextField tf, String property) {
    if (file != null) {
      String path = file.getPath().replaceAll("\\\\", "/");
      if (!path.endsWith("/")) {
        path = path + "/";
      }
      tf.setText(path);
      setProperty(property, path);
    }
  }

  private double getMaxRegionWidth(List<Region> list) {
    double max = 0;
    for (Region region : list) {
      double width = region.getWidth();
      if (width > max) {
        max = width;
      }
    }
    return max;
  }

  private void createFolders() {
    mkdir(PathUtil.DOWNLOAD);
    mkdir(PathUtil.CONFIG);
  }

  private void mkdir(String path) {
    File file = new File(path);
    if (!file.exists()) {
      String name = file.getName();
      if (file.mkdirs()) {
        System.out.println("创建 " + name + " 文件夹成功");
      } else {
        System.out.println("创建 " + name + " 文件夹失败");
      }
    }
  }

  private void setProperty(String property, String value) {
    File file = new File(PathUtil.CONFIG, PropUtil.CONFIG_FILE);
    if (file.exists()) {
      Properties prop = new Properties();
      try {
        prop.load(new BufferedReader(new FileReader(file)));
        try (PrintWriter writer = new PrintWriter(file)) {
          prop.setProperty(property, value);
          prop.store(writer, null);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
        writer.println(property + " = " + value);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
