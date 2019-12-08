package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.i18n.I18n;
import com.github.czy211.wowapi.model.WikiPage;
import com.github.czy211.wowapi.util.Utils;
import javafx.scene.control.Label;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class WikiAPIPane extends APIPane {
    private WikiPage page;

    public WikiAPIPane(WikiPage page) {
        this.page = page;
        getLabel().setText(page.getName());
        checkStatus();
    }

    @Override
    public void checkForUpdate() {
        String fileName = page.getFileName();
        String outputFile = Utils.getOutputDirectory() + "/" + fileName;
        File file = new File(outputFile);
        String url = Constants.WIKI_BASE_URL + page.getPath();
        try {
            Document document = Jsoup.connect(url).get();
            long timestamp = Utils.getTimestamp(document);
            if (!file.exists() || getTimestampFromFile(file) < timestamp) {
                updateStatus(I18n.getText("status_update_available"));
            } else {
                updateStatus(I18n.getText("status_latest_version") + "        version: "
                        + Utils.convertTimestampToString(timestamp));
            }
        } catch (IOException e) {
            updateStatus(I18n.getText("status_connect_fail", url));
            e.printStackTrace();
        }
    }

    @Override
    public void download() {
        String fileName = page.getFileName();
        String outputFile = Utils.getOutputDirectory() + "/" + fileName;
        try {
            String content = page.crawl();
            if (content != null && !"".equals(content)) {
                PrintWriter output = new PrintWriter(outputFile);
                output.println(page.crawl());
                updateStatus(I18n.getText("status_download_finished") + "        version: "
                        + Utils.convertTimestampToString(page.getTimestamp()));
                output.close();
            }
        } catch (IOException e) {
            updateStatus(I18n.getText("status_connect_fail", Constants.WIKI_BASE_URL + page.getPath()));
            e.printStackTrace();
        }
    }

    @Override
    public void checkStatus() {
        Label status = getStatus();
        String fileName = page.getFileName();
        File file = new File(Utils.getOutputDirectory() + "/" + fileName);
        if (!file.exists()) {
            status.setText(I18n.getText("status_file_not_exist", fileName + " "));
        } else {
            status.setText("version: " + Utils.convertTimestampToString(getTimestampFromFile(file)));
        }
    }

    private long getTimestampFromFile(File file) {
        try (Scanner in = new Scanner(file)) {
            return Long.parseLong(in.nextLine().substring(Constants.COMMENT_TIMESTAMP.length()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
