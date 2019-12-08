package com.github.czy211.wowapi.model;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class FXMLPage extends APIPage {
    private String[] fileNames;
    private int build;

    public FXMLPage(String name) {
        super(name);
        fileNames = new String[]{"ArtTextureID.lua", "AtlasInfo.lua", "GlobalStrings.txt"};
    }

    @Override
    public void download() throws IOException {
        for (String fileName : fileNames) {
            StringBuilder content = new StringBuilder();
            // 获取远程 build 号
            Document document = Jsoup.connect(Constants.FXML_BASE_URL + "/live").get();
            build = Utils.getBuild(document);
            URL url;
            if ("GlobalStrings.txt".equals(fileName)) {
                url = new URL(Constants.FXML_BASE_URL + "/" + build + "/GlobalStrings.lua/TW/get");
            } else {
                url = new URL(Constants.FXML_BASE_URL + "/" + build + "/Helix/" + fileName + "/get");
            }
            InputStream inputStream;
            try {
                // 读取文件内容，需要添加 header 属性 “Referer” 和 “User-Agent” 才能访问
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Referer", Constants.FXML_BASE_URL + "/" + build);
                conn.setRequestProperty("User-Agent", Constants.USER_AGENT);
                inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new IOException(url.getHost() + url.getFile(), e);
            }
            // 内容不为空时，才创建文件并写入内容
            if (content.length() > 0) {
                String outputFile = Utils.getOutputDirectory() + "/" + fileName;
                try (PrintWriter output = new PrintWriter(outputFile)) {
                    output.println(Constants.COMMENT_BUILD + build + "\n");
                    output.println(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public int getBuild() {
        return build;
    }
}
