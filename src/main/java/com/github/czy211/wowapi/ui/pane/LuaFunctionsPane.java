package com.github.czy211.wowapi.ui.pane;

import java.io.IOException;

public class LuaFunctionsPane extends WikiPane {
  private static final String name = "Lua_Functions";
  private static final String extension = ".lua";
  private static final String url = BASE_URI + "/wiki/Lua_functions";
  private static final LuaFunctionsPane pane = new LuaFunctionsPane();

  private LuaFunctionsPane() {
    super(name, extension, url);
  }

  public static LuaFunctionsPane getInstance() {
    return pane;
  }

  @Override
  public void download() {
    resolve("", "dd:has([title^=API ]):not(:matches(deprecated)),span#Coroutine_Functions", "span", null);
  }

  @Override
  public String getRemoteVersion() throws IOException {
    return getPageDateTime();
  }
}
