package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.i18n.I18n;
import com.github.czy211.wowapi.model.WikiPage;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class WikiAPIPane extends APIPane {
    private WikiPage page;

    public WikiAPIPane(WikiPage page) {
        super(page);
        this.page = page;
        setStatusText();
    }

    @Override
    public void checkForUpdate() {
        String fileName = page.getFileName();
        String outputFile = Utils.getOutputDirectory() + "/" + fileName;
        File file = new File(outputFile);
        String url = Constants.WIKI_BASE_URL + page.getPath();
        try {
            // 获取远程的时间戳
            Document document = Jsoup.connect(url).get();
            long timestamp = Utils.getTimestamp(document);
            if (!file.exists() || getTimestampFromFile(file) < timestamp) {
                // 如果文件不存在或本地的时间戳小于远程的时间戳，则提示可更新
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
    public void setStatusText() {
        String fileName = page.getFileName();
        File file = new File(Utils.getOutputDirectory() + "/" + fileName);
        if (!file.exists()) {
            updateStatus(I18n.getText("status_file_not_exist", fileName + " "));
        } else {
            updateStatus("version: " + Utils.convertTimestampToString(getTimestampFromFile(file)));
        }
    }

    /**
     * 获取本地文件的时间戳
     *
     * @param file 本地文件
     * @return 时间戳
     */
    private long getTimestampFromFile(File file) {
        try (Scanner in = new Scanner(file)) {
            return Long.parseLong(in.nextLine().substring(Constants.COMMENT_TIMESTAMP.length()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
