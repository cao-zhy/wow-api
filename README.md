# 魔兽世界 API

爬取 [wowpedia](https://wow.gamepedia.com) 的数据生成lua文件，与 [wow-ui-resource](https://github.com/Gethe/wow-ui-source) 一起可以作为开发魔兽世界插件的 SDK。

## 使用
使用 java -jar wow-api-1.0.0.jar 命令（需要 JDK 1.8+ ）或者解压 wow-api.zip 得到 wow-api 文件夹，将 wow-api 文件夹添加到 SDK 的 classpath 中。

## 生成的文件说明：
- GlobalFunctions.lua：根据 https://wow.gamepedia.com/World_of_Warcraft_API 生成，不包括 UI 函数、REMOVED 函数、Public Test Realm 函数和 Classic Specific Functions
- LuaFunctions.lua：根据 https://wow.gamepedia.com/Lua_functions 生成
- WidgetFunctions.lua：根据 https://wow.gamepedia.com/Widget_API 生成，添加了 Widget 类型
