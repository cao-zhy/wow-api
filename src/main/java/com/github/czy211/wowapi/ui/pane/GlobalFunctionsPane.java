package com.github.czy211.wowapi.ui.pane;

import java.io.IOException;

public class GlobalFunctionsPane extends WikiPane {
  private static final String name = "Global_Functions";
  private static final String extension = ".lua";
  private static final String url = BASE_URI + "/wiki/World_of_Warcraft_API";
  private static final GlobalFunctionsPane pane = new GlobalFunctionsPane();

  private GlobalFunctionsPane() {
    super(name, extension, url);
  }

  public static GlobalFunctionsPane getInstance() {
    return pane;
  }

  @Override
  public void download() {
    resolve("", "dd:not(:matches(^(UI|PROTECTED|DEPRECATED))):has([title^=API ]),span#Classic", "span", null);
  }

  @Override
  public String getRemoteVersion() throws IOException {
    return getPageDateTime();
  }
}
