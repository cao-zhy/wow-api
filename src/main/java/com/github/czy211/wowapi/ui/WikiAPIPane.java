package com.github.czy211.wowapi.ui;

import com.github.czy211.wowapi.constant.Constants;
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

        Label status = getStatus();
        String fileName = page.getFileName();
        String outputFile = Utils.getOutputDirectory() + "/" + fileName;
        File file = new File(outputFile);

        Runnable runCheck = () -> {
            String url = Constants.WIKI_BASE_URL + page.getPath();
            updateStatus(status, "检查更新中……");
            try {
                Document document = Jsoup.connect(url).get();
                long timestamp = Utils.getTimestamp(document);
                if (!file.exists() || getTimestampFromFile(file) < timestamp) {
                    updateStatus(status, "有新版本可下载");
                } else {
                    updateStatus(status, "已是最新版本        version: " + Utils.convertTimestampToString(timestamp));
                }
            } catch (IOException e) {
                updateStatus(status, "无法连接到 " + url);
            }
        };
        getCheck().setOnAction(event -> new Thread(runCheck).start());

        Runnable runDownload = () -> {
            updateStatus(status, "下载中……");
            try {
                String content = page.crawl();
                if (content != null && !"".equals(content)) {
                    PrintWriter output = new PrintWriter(outputFile);
                    output.println(page.crawl());
                    updateStatus(status, "下载完成        version: " + Utils.convertTimestampToString(page
                            .getTimestamp()));
                    output.close();
                }
            } catch (IOException e) {
                updateStatus(status, "无法连接到 " + Constants.WIKI_BASE_URL + page.getPath());
            }
        };
        getDownload().setOnAction(event -> new Thread(runDownload).start());
    }

    @Override
    public void checkStatus() {
        Label status = getStatus();
        String fileName = page.getFileName();
        File file = new File(Utils.getOutputDirectory() + "/" + fileName);
        if (!file.exists()) {
            status.setText(fileName + " 不存在");
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
