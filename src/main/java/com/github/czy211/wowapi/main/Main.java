package com.github.czy211.wowapi.main;

import com.github.czy211.wowapi.model.Page;
import com.github.czy211.wowapi.util.Utils;

class Main {
    public static void main(String[] args) {
        Page WoWAPI = new Page("function", "/World_of_Warcraft_API", "WoWAPI.lua", "AttemptToSaveBindings");
        WoWAPI.crawl();

        Page widgetAPI = new Page("function", "/Widget_API", "WidgetAPI.lua");
        widgetAPI.crawl();
        String srcFile = widgetAPI.getOutputPath() + "/wow-api/" + widgetAPI.getFileName();
        Utils.addHeader(srcFile, Utils.getWidgetTypes() + "\n" + Utils.replaceFontInstance(srcFile));

        Page luaAPI = new Page("function", "/Lua_functions", "LuaAPI.lua");
        luaAPI.crawl();
    }
}
