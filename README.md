# World of Warcraft API

使 IntelliJ IDEA 插件和 VSCode 插件 EmmyLua 的代码补全提示显示暴雪定义的全局变量和函数。

## 安装说明

免安装，解压即可使用。

## 使用说明

必须已安装 jre1.8+（jre 中未包含 JavaFX 时还需要安装 JavaFX），并正确设置环境变量 JAVA_HOME。

进入解压后程序根目录下的 bin 文件夹，双击 startup.bat 启动程序，成功运行后的程序主窗口如下图所示：  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/MainWindow.png)

默认下载位置是程序根目录下的 downloads 文件夹，可以点击“选择文件夹”改变下载位置。（建议使用导出暴雪接口代码自动创建的 BlizzardInterfaceCode 文件夹，导出暴雪接口代码见 [其它说明](#导出暴雪接口代码)）

- Global_Functions.lua 文件包含了 <https://wowpedia.fandom.com/wiki/World_of_Warcraft_API> 列出的函数。
- Widget_Functions.lua 文件包含了 <https://wowpedia.fandom.com/wiki/Widget_API> 列出的函数。
- Lua_Functions.lua 文件包含了 <https://wowpedia.fandom.com/wiki/Lua_functions> 列出的函数。
- Global_Numbers.lua 文件从 <https://github.com/czy211/wow-api/releases> 下载，包含大部分数字常量。
- Global_Frames.lua 文件解析暴雪接口代码，获取游戏版本号需要连接至 <https://www.townlong-yak.com/framexml/live>，包含了大部分 Frame。（不包括角色选择界面的 Frame）
  > 暴雪接口代码的位置需要选择至 Interface 文件夹。
- Widget_Hierarchy.png 文件从 <https://wowpedia.fandom.com/wiki/File:Widget_Hierarchy.png> 下载，显示 Widget Hierarchy。
- Script_Handlers.txt 文件包含了 <https://wowpedia.fandom.com/wiki/Widget_script_handlers> 的内容。
- Global_Strings.txt 从 <https://www.townlong-yak.com/framexml/live> 下载 GlobalStrings.lua 文件，可以选择不同的语言版本。
- Art_Texture_ID.txt 从 <https://www.townlong-yak.com/framexml/live> 下载 ArtTextureID.lua 文件。
- Atlas_Info.txt 从 <https://www.townlong-yak.com/framexml/live> 下载 AtlasInfo.lua 文件。

从 wowpedia 下载的函数不包括被标记为 UI、PROTECTED、DEPRECATED、REMOVED 的函数、怀旧服函数和测试服函数。

非 lua 文件与代码补全提示无关，仅方便查询，可选择下载。

## 其它说明

### 导出暴雪接口代码

1. 在战网的游戏设置里勾选额外命令行参数，输入 -console。
   ![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/BNetSettings.png)
2. 在游戏角色选择界面按下“~”键，输入 exportinterfacefiles code。

导出的代码在 %WoW_HOME%\\_retail\_\BlizzardInterfaceCode\Interface 文件夹。

### 使用 IntelliJ IDEA 编写魔兽世界插件

1. 下载并安装 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 。
2. 安装 EmmyLua 和 WoW TOC 插件。
3. 设置 EmmyLua。  
   ![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/EmmyLuaSettings.png)
4. 导出暴雪接口代码。
5. 使用本程序下载 API 文件。
6. 创建 Lua 项目，在 Project Structure 中添加 SDK，classpath 选择 %WoW_HOME%\\_retail\_\BlizzardInterfaceCode，在 Project SDK 中选择添加的
   SDK。   
   ![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/AddSDK.png)
   ![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/SelectSDK.png)

如果要代码补全提示显示定义在非 _G 命名空间的函数，需要使用注解声明类型。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/UseAnnotation.png)
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/UseAnnotationParam.png)

如果需要给 Widget 的字段赋值，建议使用 @class 注解，因为使用 @type 或 @param 注解会导致其它使用该类型的地方也会有该字段的提示。  
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/UseAnnotationClass.png)
![](https://github.com/czy211/picture-library/blob/master/resources/wow-api/UseAnnotationType.png)

注解的使用请参考 <https://emmylua.github.io/annotation.html>

## 更新日志

### v3.3.0

- 调整 UI
- 修改下载的文件名
- 修正 Global_Frames.lua（原 Widgets.lua）文件错误
- 修复无法取消下载的问题

### v3.2.1

- 修复 WoW_API.lua 没有 NOCOMBAT 和 HW 函数的问题。
- 删除 Widgets.lua 中多余的注解。

### v3.2.0

- 删除枚举值提示。
- 删除可以通过使用 parentKey 引用的 widget。
- 删除没有 parentKey 的 template。
- 修改有 parentKey 的 widget 和 template 的注解为 @class。
- 修改 UI 和下载的文件名。
- 添加部分函数的返回类型。
- 修复部分 bug。

### v3.1.3

- 修正了部分以 $parent 开头的全局变量不正确的问题。

### v3.1.2

- WoW_API.lua 添加命名空间。

### v3.1.1

- 修复 Global_Variables.lua 中有错误的 widget 类型的问题。

### v3.1.0

- 优化解析暴雪接口代码，支持显示更多暴雪定义的 Widget 补全提示。

### v3.0.0

- 添加 widget hierarchy 图片下载功能。
- 添加 widget script type 的代码补全提示。
- 添加 system api 的检查更新功能。
- 添加 GlobalStrings 文件的选择语言下载功能。
- 添加导出暴雪接口代码中的全局变量和函数的功能。
- 添加取消下载的功能。
- 添加下载进度条。
