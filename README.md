# WoW API
## 项目背景
使用 IntelliJ IDEA 编写魔兽世界插件时，暴雪定义的全局变量和函数没有代码补全提示。

## 功能介绍
下载魔兽世界 API 文件，使插件 [EmmyLua](https://plugins.jetbrains.com/plugin/9768-emmylua/) 的代码补全提示显示暴雪定义的全局变量和函数。

## 安装方法
免安装，解压即可使用。

## 使用方法
必须已安装 jre1.8+，并正确设置环境变量 JAVA_HOME。

进入解压后程序根目录下的 bin 文件夹，双击 startup.bat 启动程序，成功运行后的程序主窗口如下图所示：  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Main%20Window.png)

默认下载位置是程序根目录下的 downloads 文件夹，你可以点击 “选择文件夹” 改变下载位置。（建议使用导出暴雪接口代码时创建的 BlizzardInterfaceCode 文件夹）

API 面板左边显示 API 文件名，中间显示文件信息和操作状态，右边显示可进行的操作。如下图所示：  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Download.png)

程序从 [wowpedia](https://wow.gamepedia.com/) 获取的全局函数都是正式服的非 UI、非 DEPRECATED，非 REMOVED 函数。
- Widget_Hierarchy.png 文件显示 widget hierarchy，仅供查看，不影响代码补全提示。因为没有版本信息，所以不提供检查更新。
- WoW_API.lua 文件包含了 <https://wow.gamepedia.com/World_of_Warcraft_API> 列出的函数，代码补全提示会包含这些函数。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/WoW%20API.png)
- Widget_API.lua 文件包含了 <https://wow.gamepedia.com/Widget_API> 列出的函数，代码补全提示会包含这些函数。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Widget%20API.png)
- Widget_Script_Types.lua 文件包含了 <https://wow.gamepedia.com/Widget_script_handlers/Complete_list> 列出的所有 script type，在输入 script type 时代码补全提示会包含这些 script type。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Script%20Type.png)
- Widget_Script_Handlers.lua 文件包含了 <https://wow.gamepedia.com/Widget_script_handlers> 列出的所有函数，仅供查询，不影响代码补全提示。
- Lua_API.lua 文件包含了 <https://wow.gamepedia.com/Lua_functions> 列出的所有函数，代码补全提示会包含这些函数。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Lua%20API.png)
- Global_Strings.lua 从 <https://www.townlong-yak.com/framexml/live> 下载 GlobalStrings.lua 文件，包含了所有的全局字符串常量，可以选择不同的语言版本。代码补全提示会包含这些常量，因为数据非常多，会影响代码补全提示的效率，如果只想作为查询使用，可修改文件后缀名为 txt。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Global%20Strings.png)
- Art_Texture_Id.lua 从 <https://www.townlong-yak.com/framexml/live> 下载 ArtTextureID.lua 文件，包含所有材质 id，仅供查询使用，不影响代码补全提示。
- Atlas_Info.lua 从 <https://www.townlong-yak.com/framexml/live> 下载 AtlasInfo.lua 文件，包含所有图集信息，仅供查询使用，不影响代码补全提示。
- System_API.lua 文件包含了暴雪 API 文档插件中所有的命名空间、事件和枚举值，和部分暴雪接口代码中没有的全局数字常量。代码补全提示会包含这些值。因为是使用 [WlkUI](https://github.com/czy211/wlk-ui) 插件在游戏内导出，所以不提供下载。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/System%20API.png)
- Global_Variabls.lua 从暴雪接口代码导出 xml 文件中定义的一级 widget 全局变量和 lua 文件中的 mixin 和其它全局函数，代码补全提示会包含这些值。需要提供暴雪接口代码位置，暴雪接口代码导出的默认位置是 %WoW_HOME%\\_retail\_\BlizzardInterfaceCode\Interface。因为没有版本信息，所以不提供检查更新。 
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Global%20Variables.png)

## 其它说明
### 使用 IntelliJ IDEA 编写魔兽世界插件
1. 下载 [IDEA](https://www.jetbrains.com/idea/)。
2. 下载 EmmyLua 和 WoW TOC 插件。
3. 设置 EmmyLua。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/EmmyLua%20Setting.png)
4. 导出暴雪接口代码。在战网的的游戏设置里勾选额外命令行参数，并输入 -console。在人物选择界面按下 “\`” 键，在弹出的输入框中输入 exportinterfacefiles code，导出的代码在 %WoW_HOME%\\_retail\_\BlizzardInterfaceCode\Interface 文件夹。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/Console.png)
5. 使用本程序下载 API 文件。
6. 使用 [WlkUI](https://github.com/czy211/wlk-ui) 插件导出 System API。
7. 添加 SDK。创建项目，在 Project Structure 中添加 SDK，classpath 选择 %WoW_HOME%\\_retail\_\BlizzardInterfaceCode。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/SDK.png)

widget 和 mixin 等 table 的函数需要使用注解声明变量的类型，才会有代码补全提示。例如：
```
---@type Frame
local f = CreateFrame("Frame")
```
这样使用 “f” 的时候，才会有 Frame 的函数代码补全提示。关于注解的使用请参考 <https://emmylua.github.io/annotation.html>

## 改动日志
### v3.0.0
- 添加 widget hierarchy 图片下载功能。
- 添加 widget script type 的代码补全提示。
- 添加 system api 的检查更新功能。
- 添加 GlobalStrings 文件的选择语言下载功能。
- 添加导出暴雪接口代码中的全局变量和函数的功能。
- 添加取消下载的功能。
- 添加下载进度条。
