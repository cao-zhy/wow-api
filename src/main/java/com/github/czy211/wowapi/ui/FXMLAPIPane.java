package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.model.FXMLPage;
import com.github.czy211.wowapi.util.Utils;
import javafx.scene.control.Label;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FXMLAPIPane extends APIPane {
    private FXMLPage page;
    private String[] statusText;

    public FXMLAPIPane(FXMLPage page) {
        this.page = page;

        getLabel().setText(page.getName());

        checkStatus();
        Label status = getStatus();

        Runnable runCheck = () -> {
            String url = Constants.FXML_BASE_URL + "/live";
            updateStatus(status, "检查更新中……");
            try {
                Document document = Jsoup.connect(url).get();
                int build = Utils.getBuild(document);
                int statusType = getStatusType();
                if (statusType == 0 || statusType == 2 || getBuildFromFile(new File(
                        Utils.getOutputDirectory() + "/" + page.getFileNames()[0])) < build) {
                    updateStatus(status, "有新版本可下载");
                } else {
                    updateStatus(status, "已是最新版本        build: " + build);
                }
            } catch (IOException e) {
                updateStatus(status, "无法连接到 " + url);
            }
        };
        getCheck().setOnAction(event -> new Thread(runCheck).start());

        Runnable runDownload = () -> {
            updateStatus(status, "下载中……");
            try {
                page.download();
                updateStatus(status, "下载完成        build: " + page.getBuild());
            } catch (IOException e) {
                updateStatus(status, "无法连接到 " + e.getMessage());
                e.printStackTrace();
            }
        };
        getDownload().setOnAction(event -> new Thread(runDownload).start());
    }

    @Override
    public void checkStatus() {
        int statusType = getStatusType();
        getStatus().setText(statusText[statusType]);
    }

    private int getStatusType() {
        StringBuilder text = new StringBuilder();
        int build = -1;
        boolean buildIsDiff = false;
        String[] fileNames = page.getFileNames();
        for (String fileName : fileNames) {
            File file = new File(Utils.getOutputDirectory() + "/" + fileName);
            if (!file.exists()) {
                text.append(fileName).append(" ");
            } else if (!buildIsDiff) {
                int fileBuild = getBuildFromFile(file);
                if (build != -1 && build != fileBuild) {
                    buildIsDiff = true;
                }
                build = fileBuild;
            }
        }
        statusText = new String[]{text + "不存在", "build: " + build, "版本不一致"};
        if (text.length() > 0) {
            return 0;
        } else if (buildIsDiff) {
            return 2;
        }
        return 1;
    }

    private int getBuildFromFile(File file) {
        try (Scanner in = new Scanner(file)) {
            return Integer.parseInt(in.nextLine().substring(Constants.COMMENT_BUILD.length()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
