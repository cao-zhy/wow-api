package com.github.czy211.wowapi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtil {
  public static final String CONFIG;
  public static final String DOWNLOAD;

  static {
    String path = PathUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    Matcher m = Pattern.compile("(.*)/[^/]*/[^/]*$").matcher(path);
    String classpath = "";
    if (m.find()) {
      classpath = m.group(1);
    }
    DOWNLOAD = classpath + "/downloads/";
    CONFIG = classpath + "/conf/";
  }

  public static String getDownloadPath() {
    String path = PropUtil.getProperty(PropUtil.DOWNLOAD_PATH);
    return path == null ? DOWNLOAD : path;
  }

}
