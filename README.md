# 魔兽世界API

爬取[wowpedia](https://wow.gamepedia.com)的数据生成lua文件，与[wow-ui-resource](https://github.com/Gethe/wow-ui-source)一起可以作为开发魔兽世界插件的SDK。

## 使用
使用java -jar wow-api-1.0.0.jar命令（需要JDK1.8+）或者解压wow-api.zip得到wow-api文件夹，将wow-api文件夹添加到SDK的classpath中。

## 生成的文件说明：
- GlobalFunctions.lua: 根据https://wow.gamepedia.com/World_of_Warcraft_API生成，不包括UI函数、REMOVED函数、Public Test Realm函数和Classic Specific Functions
- LuaFunctions.lua: 根据https://wow.gamepedia.com/Lua_functions生成
- WidgetFunctions.lua: 根据https://wow.gamepedia.com/Widget_API生成，添加了Widget类型
