package com.github.czy211.wowapi.util;

import com.github.czy211.wowapi.constant.PathConst;
import com.github.czy211.wowapi.constant.PropConst;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Utils {
    public static String getDownloadPath() {
        File configFilePath = new File(PathConst.CONFIG_FILE);
        if (configFilePath.exists() && configFilePath.isFile()) {
            Properties properties = new Properties();
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                String path = properties.getProperty(PropConst.DOWNLOAD_PATH);
                return path == null ? PathConst.DEFAULT_DOWNLOAD : path;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return PathConst.DEFAULT_DOWNLOAD;
    }
}
