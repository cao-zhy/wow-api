package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.util.Utils;
import javafx.application.Platform;
import javafx.scene.control.Button;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class WidgetHierarchyPane extends BaseApiPane {
    public WidgetHierarchyPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        String urlStr = "https://gamepedia.cursecdn.com/wowpedia/c/ca/Widget_Hierarchy.png";
        Button btDownload = getBtDownload();
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            int total = connection.getContentLength();
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(Utils.getDownloadPath()
                         + getName()))) {
                btDownload.setDisable(false);
                Platform.runLater(() -> btDownload.setText("取消下载"));

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
                    updateProgress((double) current / total);
                }
            }
        } catch (IOException e) {
            throw new IOException(urlStr, e);
        }
    }

    @Override
    public long getRemoteVersion() {
        return 0;
    }
}
