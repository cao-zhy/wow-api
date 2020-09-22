package com.github.czy211.wowapi.util;

import com.github.czy211.wowapi.constant.PathConst;
import com.github.czy211.wowapi.constant.PropConst;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Utils {
    public static String getDownloadPath() {
        String path = getConfigProperty(PropConst.DOWNLOAD_PATH);
        return path == null ? PathConst.DEFAULT_DOWNLOAD : path;
    }

    public static String getBicPath() {
        return getConfigProperty(PropConst.BIC_PATH);
    }

    public static String getConfigProperty(String property) {
        File configFilePath = new File(PathConst.CONFIG_FILE);
        if (configFilePath.exists() && configFilePath.isFile()) {
            Properties properties = new Properties();
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                return properties.getProperty(property);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void setConfigProperty(String property, String value) {
        File configFilePath = new File(PathConst.CONFIG_FILE);
        if (configFilePath.exists()) {
            Properties properties = new Properties();
            try {
                // 不加载 properties 就写入的话会清空之前的属性
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFilePath),
                        StandardCharsets.UTF_8));
                properties.load(reader);
                try (PrintWriter writer = new PrintWriter(configFilePath, "UTF-8")) {
                    properties.setProperty(property, value);
                    properties.store(writer, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 创建配置文件并写入属性
            try (PrintWriter writer = new PrintWriter(configFilePath, "UTF-8")) {
                writer.println(property + "=" + value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
