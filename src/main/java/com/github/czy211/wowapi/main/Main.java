package com.github.czy211.wowapi.main;

import com.github.czy211.wowapi.model.FunctionGroup;
import com.github.czy211.wowapi.util.Utils;

class Main {
    public static void main(String[] args) {
        FunctionGroup global = new FunctionGroup("/World_of_Warcraft_API", "GlobalFunctions.lua",
                "AttemptToSaveBindings");
        global.crawl();

        FunctionGroup widget = new FunctionGroup("/Widget_API", "WidgetFunctions.lua");
        widget.crawl();
        if (widget.getOutputPath() != null) {
            String src = widget.getOutputPath() + "/wow-api/" + widget.getFileName();
            Utils.addHeader(src, Utils.generateWidgetTypesAndFunc(src));
        }

        FunctionGroup lua = new FunctionGroup("/Lua_functions", "LuaFunctions.lua");
        lua.crawl();
    }
}
