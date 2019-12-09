# World of Warcraft API
下载魔兽世界 API 文件。

## 安装
下载 wow-api-2.0.1-bin.zip 文件，解压至任意目录。

## 使用
需要安装 JDK 1.8+，并正确设置环境变量 JAVA_HOME。
- 进入 wow-api-2.0.1\bin 目录，运行 startup.bat 文件启动程序。
- 点击选择文件夹按钮可自定义下载位置，默认位置是 wow-api-2.0.1\downloads。
- 点击打开文件夹按钮会打开下载位置。
- 状态标签会显示本地文件状态。
- 点击检查更新按钮会比较对应的本地文件版本号和远程版本号。
- 点击下载按钮会下载对应的文件。
- WoW API 行对应的是 [https://wow.gamepedia.com/World_of_Warcraft_API](https://wow.gamepedia.com/World_of_Warcraft_API) 页面所有正式服的函数。
- Lua API 行对应的是 [https://wow.gamepedia.com/Lua_functions](https://wow.gamepedia.com/Lua_functions) 页面所有函数。
- Widget API 行对应的是 [https://wow.gamepedia.com/Widget_API](https://wow.gamepedia.com/Widget_API) 页面所有函数。
- Widget Handlers 行对应的是 [https://wow.gamepedia.com/Widget_handlers](https://wow.gamepedia.com/Widget_handlers) 页面所有函数。
- Frame XML 行对应的是 [https://www.townlong-yak.com/framexml/live](https://www.townlong-yak.com/framexml/live) 的 ArtTextureID.lua、AtlasInfo.lua 和 GlobalStrings.lua（TW） 文件。
- WoW API、Lua API 和 Widget API 创建的文件配合 IntelliJ 插件 [EmmyLua](https://plugins.jetbrains.com/plugin/9768-emmylua/) 会提示文件中的全局变量和函数。
- Widget Handlers 和 FrameXML 创建的文件只提供查询。
