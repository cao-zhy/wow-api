package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.i18n.I18n;
import com.github.czy211.wowapi.model.FXMLPage;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FXMLAPIPane extends APIPane {
    private FXMLPage page;
    private String[] statusText;

    public FXMLAPIPane(FXMLPage page) {
        this.page = page;
        getLabel().setText(page.getName());
        checkStatus();
    }

    @Override
    public void checkForUpdate() {
        String url = Constants.FXML_BASE_URL + "/live";
        try {
            // 获取远程 build 号
            Document document = Jsoup.connect(url).get();
            int build = Utils.getBuild(document);
            // 获取状态类型
            int statusType = getStatusType();
            // 有文件不存在或版本不一致或本地 build 号小于远程 build 号时提示可以更新
            if (statusType == 0 || statusType == 2 || getBuildFromFile(new File(
                    Utils.getOutputDirectory() + "/" + page.getFileNames()[0])) < build) {
                updateStatus(I18n.getText("status_update_available"));
            } else {
                updateStatus(I18n.getText("status_latest_version") + "        build: " + build);
            }
        } catch (IOException e) {
            updateStatus(I18n.getText("status_connect_fail", url));
            e.printStackTrace();
        }
    }

    @Override
    public void download() {
        try {
            page.download();
            updateStatus(I18n.getText("status_download_finished") + "        build: " + page.getBuild());
        } catch (IOException e) {
            updateStatus(I18n.getText("status_connect_fail", e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public void checkStatus() {
        int statusType = getStatusType();
        getStatus().setText(statusText[statusType]);
    }

    /**
     * 获取状态类型
     *
     * @return 状态类型
     */
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
        statusText = new String[]{I18n.getText("status_file_not_exist", text), "build: " + build,
                I18n.getText("status_version_different")};
        if (text.length() > 0) {
            // 有文件不存在
            return 0;
        } else if (buildIsDiff) {
            // 版本不一致
            return 2;
        }
        // 文件都存在且版本一致
        return 1;
    }

    /**
     * 获取本地文件的 build 号
     *
     * @param file 本地文件
     * @return build 号
     */
    private int getBuildFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            return Integer.parseInt(reader.readLine().substring(Constants.COMMENT_BUILD.length()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
