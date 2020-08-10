package com.github.czy211.wowapi.constant;

public class PathConst {
    public static final String ROOT_PATH;
    public static final String CONFIG;
    public static final String CONFIG_FILE;
    public static final String DEFAULT_DOWNLOAD;

    static {
        String path = PathConst.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        int secondLastSlashIndex = path.lastIndexOf("/", lastSlashIndex - 1);
        ROOT_PATH = path.substring(0, secondLastSlashIndex + 1);
        CONFIG = ROOT_PATH + "conf/";
        CONFIG_FILE = CONFIG + "config.properties";
        DEFAULT_DOWNLOAD = ROOT_PATH + "downloads/";
    }
}
