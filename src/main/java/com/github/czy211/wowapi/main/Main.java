package com.github.czy211.wowapi.main;

import com.github.czy211.wowapi.model.Page;
import com.github.czy211.wowapi.util.Utils;

class Main {
    public static void main(String[] args) {
        Page WoWAPI = new Page("function", "/World_of_Warcraft_API", "WoWAPI.lua", "AttemptToSaveBindings");
        String output = WoWAPI.crawl();
        Utils.addHeader(output, "--- timestamp: " + WoWAPI.getTimestamp() + "\n\n");

        Page widgetAPI = new Page("function", "/Widget_API", "WidgetAPI.lua");
        output = widgetAPI.crawl();
        Utils.addHeader(output, "--- timestamp: " + widgetAPI.getTimestamp() + "\n\n" + Utils.getWidgetTypes() + "\n"
                + Utils.replaceFontInstance(output));

        Page luaAPI = new Page("function", "/Lua_functions", "LuaAPI.lua");
        output = luaAPI.crawl();
        Utils.addHeader(output, "--- timestamp: " + luaAPI.getTimestamp() + "\n\n");

        Page widgetHandler = new Page("handler", "/Widget_handlers", "WidgetHandler.lua");
        output = widgetHandler.crawl();
        Utils.addHeader(output, "--- timestamp: " + widgetHandler.getTimestamp() + "\n\n" + Utils.getWidgetTypes()
                + "\n\n");
    }
}
