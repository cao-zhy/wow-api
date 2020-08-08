package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.util.Utils;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class WidgetHierarchyPane extends BaseApiPane {
    public WidgetHierarchyPane(String name) {
        super(name);
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public void download() throws IOException {
        Label lbStatus = getLbStatus();
        ProgressBar progressBar = getProgressBar();
        String urlStr = "https://gamepedia.cursecdn.com/wowpedia/c/ca/Widget_Hierarchy.png";
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            int total = connection.getContentLength();
            if (!Thread.currentThread().isInterrupted()) {
                try (InputStream in = new BufferedInputStream(connection.getInputStream());
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(Utils.getDownloadPath()
                             + getName()))) {
                    byte[] data = new byte[8192];
                    int current = 0;
                    int length;
                    while ((length = in.read(data)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        out.write(data, 0, length);
                        out.flush();
                        current += length;
                        double progress = (double) current / total;
                        String status = String.format("%s %.1f%s", "下载中……", progress * 100, "%");
                        Platform.runLater(() -> {
                            progressBar.setProgress(progress);
                            lbStatus.setText(status);
                        });
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException(urlStr, e);
        }
    }
}
