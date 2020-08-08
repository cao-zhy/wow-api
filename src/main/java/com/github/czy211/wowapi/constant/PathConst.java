package com.github.czy211.wowapi.constant;

import java.io.File;

public class PathConst {
    public static final String ROOT_PATH;
    public static final String CONFIG;
    public static final String CONFIG_FILE;
    public static final String DEFAULT_DOWNLOAD;

    static {
        String path = PathConst.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File file = new File(path);
        if (path.endsWith(".jar") && file.isFile()) {
            ROOT_PATH = path.substring(0, path.lastIndexOf("/") + 1);
        } else {
            ROOT_PATH = path;
        }
        CONFIG = ROOT_PATH + "conf/";
        CONFIG_FILE = CONFIG + "config.properties";
        DEFAULT_DOWNLOAD = ROOT_PATH + "downloads/";
    }
}