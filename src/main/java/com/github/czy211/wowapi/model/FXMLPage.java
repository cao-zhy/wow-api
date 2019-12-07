package com.github.czy211.wowapi.model;

import com.github.czy211.wowapi.constant.Constants;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FXMLPage {
    private String name;
    private String[] fileNames;
    private int build;

    public FXMLPage() {
        name = "FrameXML";
        fileNames = new String[]{"ArtTextureID.lua", "AtlasInfo.lua", "GlobalStrings.txt"};
    }

    public void download() throws IOException {
        for (String fileName : fileNames) {
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
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Referer", Constants.FXML_BASE_URL + "/" + build);
                conn.setRequestProperty("User-Agent", Constants.USER_AGENT);
                inputStream = conn.getInputStream();
            } catch (IOException e) {
                throw new IOException(url.getHost() + url.getFile(), e);
            }
            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(Utils.getOutputDirectory()
                    + "/" + fileName))) {
                outputStream.writeBytes(Constants.COMMENT_BUILD + build + "\n\n");
                byte[] buffer = new byte[1000];
                int readBytes;
                while ((readBytes = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public int getBuild() {
        return build;
    }
}