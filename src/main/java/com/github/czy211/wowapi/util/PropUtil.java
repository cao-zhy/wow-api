package com.github.czy211.wowapi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropUtil {
    public static final String DOWNLOAD_PATH = "download_path";
    public static final String CODE_PATH = "code_path";
    public static final String CONFIG_FILE = "config.properties";
    
    public static String getProperty(String property) {
        File file = new File(PathUtil.CONFIG, CONFIG_FILE);
        if (file.exists()) {
            Properties prop = new Properties();
            try {
                prop.load(new BufferedReader(new FileReader(file)));
                return prop.getProperty(property);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
