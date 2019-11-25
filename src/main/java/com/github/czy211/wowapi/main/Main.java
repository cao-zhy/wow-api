package com.github.czy211.wowapi.main;

import com.github.czy211.wowapi.model.Page;
import com.github.czy211.wowapi.util.Utils;

class Main {
    public static void main(String[] args) {
        Page global = new Page("function", "/World_of_Warcraft_API", "GlobalFunctions.lua", "AttemptToSaveBindings");
        global.crawl();

        Page widget = new Page("function", "/Widget_API", "WidgetFunctions.lua");
        widget.crawl();
        if (widget.getOutputPath() != null) {
            String src = widget.getOutputPath() + "/wow-api/" + widget.getFileName();
            Utils.addHeader(src, Utils.generateWidgetTypesAndFunc(src));
        }

        Page lua = new Page("function", "/Lua_functions", "LuaFunctions.lua");
        lua.crawl();
    }
}
