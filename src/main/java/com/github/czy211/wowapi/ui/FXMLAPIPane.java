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
            Document document = Jsoup.connect(url).get();
            int build = Utils.getBuild(document);
            int statusType = getStatusType();
            if (statusType == 0 || statusType == 2 || getBuildFromFile(new File(
                    Utils.getOutputDirectory() + "/" + page.getFileNames()[0])) < build) {
                updateStatus(I18n.getText("status_update_available"));
            } else {
                updateStatus(I18n.getText("status_latest_build", build));
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
            updateStatus(I18n.getText("status_fxml_download_finished", page.getBuild()));
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
            return 0;
        } else if (buildIsDiff) {
            return 2;
        }
        return 1;
    }

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
