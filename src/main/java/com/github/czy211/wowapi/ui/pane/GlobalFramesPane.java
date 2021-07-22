package com.github.czy211.wowapi.ui.pane;

import com.github.czy211.wowapi.util.PathUtil;
import com.github.czy211.wowapi.util.PropUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalFramesPane extends BasePane {
  private static final String name = "Global_Frames";
  private static final String extension = ".lua";
  private static final String url = FxmlPane.FXML_URL;
  private static final GlobalFramesPane pane = new GlobalFramesPane();
  private final TextField tfCode;
  private final Button btSelect;
  private final HashMap<String, Template> templates = new HashMap<>();

  private GlobalFramesPane() {
    super(name, extension, url);
    Label lbCode = new Label("BlizzardInterfaceCode");
    tfCode = new TextField();
    btSelect = new Button("选择文件夹");
    HBox top = new HBox(5, lbCode, tfCode, btSelect);
    setTop(top);
    top.setAlignment(Pos.CENTER);
    HBox.setHgrow(tfCode, Priority.ALWAYS);
    setMargin(top, new Insets(0, 0, 5, 0));
    tfCode.setEditable(false);
    tfCode.setFocusTraversable(false);
    String codePath = getCodePath();
    if (codePath == null) {
      getBtDownload().setDisable(true);
    } else {
      tfCode.setText(codePath);
    }
  }

  public static GlobalFramesPane getInstance() {
    return pane;
  }

  @Override
  public void download() {
    File file = new File(getCodePath());
    StringBuilder result = new StringBuilder();
    List<String> list = new ArrayList<>();
    connected(getFileSize(file) * 2);
    addTemplates(file, result, list);
    try (PrintWriter writer = new PrintWriter(PathUtil.getDownloadPath() + "Templates.lua", "UTF-8")) {
      writer.print(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
    result.delete(0, result.length());
    addFrames(file, result);
    createFile(result);
  }

  @Override
  public String getRemoteVersion() throws IOException {
    Element element = Jsoup.connect(FxmlPane.FXML_URL).get().selectFirst("h1");
    String build = element.text().substring(6, 11);
    Element el = element.selectFirst(".morebuilds");
    if (el != null) {
      String title = el.attr("title");
      build = title.substring(1);
    }
    return build;
  }

  public Button getBtSelect() {
    return btSelect;
  }

  public TextField getTfCode() {
    return tfCode;
  }

  private void addFrames(File path, StringBuilder sb) {
    File[] files = path.listFiles();
    if (files != null) {
      for (File file : files) {
        String filename = file.getName();
        if (file.isDirectory()) {
          if (isExcludeDir(filename)) {
            continue;
          }
          addFrames(file, sb);
        } else {
          if (filename.endsWith(".xml")) {
            try {
              BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
              Elements elements = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser()).select("[name]:not([name^=$],"
                  + "[virtual=true],[intrinsic=true],[parentKey],Binding,Font,Attribute)");
              for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                  canceled();
                  return;
                }
                String name = element.attr("name");
                addFrame(sb, element, name);
                addChildFrames(sb, element, name);
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          increaseCurrent(getFileSize(file));
        }
      }
    }
  }

  private void addFrame(StringBuilder sb, Element element, String prefix) {
    String name = element.attr("name");
    String frameName = getFrameName(element, prefix);
    if (ownChildWithPK(element, name)) {
      sb.append(frameName).append(" = {\n");
      addChildParentKeys(sb, element, name, frameName, 1);
      sb.append("}\n");
    }
    addTypes(sb, element);
    sb.append(frameName).append(" = {}\n\n");
    String[] inherits = element.attr("inherits").split(", *");
    for (String inherit : inherits) {
      Template template = templates.get(inherit);
      if (template != null) {
        addParentChildFrames(sb, template, frameName);
      }
    }
  }

  private void addChildFrames(StringBuilder sb, Element element, String prefix) {
    Elements elements = element.select("* [name^=$parent]:not([parentKey])");
    for (Element el : elements) {
      addFrame(sb, el, prefix);
    }
  }

  private void addParentChildFrames(StringBuilder sb, Template template, String prefix) {
    Elements elements = template.getChildFrames();
    if (elements != null) {
      for (Element element : elements) {
        addFrame(sb, element, prefix);
      }
      String[] inherits = template.getInherits();
      if (inherits != null) {
        for (String inherit : inherits) {
          Template t = templates.get(inherit);
          if (t != null) {
            addParentChildFrames(sb, t, prefix);
          }
        }
      }
    }
  }

  private String getFrameName(Element element, String prefix) {
    String name = element.attr("name");
    if (name.startsWith("$parent")) {
      for (Element parent : element.parents()) {
        String parentName = parent.attr("name");
        if (!"".equals(parentName)) {
          if (parentName.startsWith("$parent")) {
            name = name.replace("$parent", parentName);
          } else {
            return name.replace("$parent", prefix);
          }
        }
      }
    }
    return name;
  }

  private void addTemplates(File path, StringBuilder result, List<String> list) {
    File[] files = path.listFiles();
    if (files != null) {
      for (File file : files) {
        String filename = file.getName();
        if (file.isDirectory()) { // 解析文件夹
          if (isExcludeDir(filename)) {
            continue;
          }
          addTemplates(file, result, list);
        } else {
          try {
            if (filename.endsWith(".lua")) { // 解析 lua 文件
              BufferedReader reader = new BufferedReader(new FileReader(file));
              String line;
              while ((line = reader.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                  canceled();
                  return;
                }
                String name = null;
                Matcher m = Pattern.compile("function[ \t]+(\\w+):\\w+[ \t]*\\(.*\\)").matcher(line);
                // 添加含有函数的全局变量
                if (m.find()) {
                  name = m.group(1);
                  if (!list.contains(name)) {
                    list.add(name);
                    result.append("---@class ").append(name).append("\n").append(name)
                        .append(" = {}\n\n");
                  }
                }
                // 添加 TickerPrototype 函数（TickerPrototype 声明为局部变量）
                if ("TickerPrototype".equals(name)) {
                  result.append(line).append(" end\n\n");
                }
                // 添加使用 _G.xxx = xxx 方式定义的函数（Blizzard_CombatLog.lua）
                m = Pattern.compile("_G\\.(\\w+)[ \t]*=[ \t]*(\\w+)$").matcher(line);
                if (m.find()) {
                  String funName = m.group(1);
                  if (funName.equals(m.group(2))) {
                    result.append("function ").append(funName).append("(...) end\n\n");
                  }
                }
              }
            } else if (filename.endsWith(".xml")) { // 解析 xml 文件
              BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
              Elements elements = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser()).select("[name]:not([name^=$],Font,"
                  + "FontFamily,Texture)[virtual=true],[intrinsic=true]");
              for (Element element : elements) {
                if (Thread.currentThread().isInterrupted()) {
                  canceled();
                  return;
                }
                Template template = new Template();
                String name = element.attr("name");
                String inherits = element.attr("inherits");
                if (!"".equals(inherits)) {
                  template.setInherits(inherits.split(", *"));
                }
                String mixins = element.attr("mixin") + element.attr("secureMixin");
                if (!"".equals(mixins)) {
                  template.setMixins(mixins.split(", *"));
                }
                Elements els = element.select("[name^=$parent]:not([parentKey])");
                if (els.size() > 0) {
                  template.setChildFrames(els);
                }
                templates.put(name, template);
                String tagName = element.tagName();
                result.append("---@class ").append(name).append(":").append(tagName).append("\n")
                    .append(name).append(" = {");
                if (ownChildWithPK(element, name)) {
                  result.append("\n");
                  addChildParentKeys(result, element, name, name, 1);
                }
                result.append("}\n\n");
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          increaseCurrent(getFileSize(file));
        }
      }
    }
  }

  private void addChildParentKeys(StringBuilder sb, Element element, String parentName, String prefix, int numTab) {
    for (Element el : element.select(">:not([parentKey])")) {
      addChildParentKeys(sb, el, parentName, prefix, numTab);
    }
    for (Element el : element.select(">[parentKey]")) {
      if (isChild(el, parentName)) {
        insertTab(sb, numTab);
        boolean own = ownChildWithPK(el, parentName);
        String pk = el.attr("parentKey");
        String className = prefix;
        if (own) {
          className += "_" + pk;
          sb.append("---@class ").append(className).append(":").append(el.tagName()).append("\n");
        } else {
          addTypes(sb, el);
        }
        insertTab(sb, numTab);
        if (pk.contains("-")) {
          pk = "[\"" + pk + "\"]";
        }
        sb.append(pk).append(" = {");
        if (own) {
          sb.append("\n");
          addChildParentKeys(sb, el, parentName, className, numTab + 1);
          insertTab(sb, numTab);
        }
        sb.append("},\n");
        if (own) {
          insertTab(sb, numTab);
          addTypes(sb, el);
          insertTab(sb, numTab);
          sb.append(pk).append(" = {},\n");
        }
      }
    }
  }

  private void addTypes(StringBuilder sb, Element element) {
    String tagName = getFrameTagName(element);
    sb.append("---@type ").append(tagName);
    // intrinsic frame 的 name 和标签名相同，在 templates 中找到 intrinsic frame 并添加它的父类类型
    Template template = templates.get(tagName);
    if (template != null) {
      addParentTypes(sb, template);
    }
    addTypes(sb, element.attr("mixin").split(", *"));
    if (!"FontString".equals(tagName) && !"Texture".equals(tagName)) {
      String[] inherits = element.attr("inherits").split(", *");
      addTypes(sb, inherits);
      // 添加父类中的 inherits 和 mixins
      for (String inherit : inherits) {
        template = templates.get(inherit);
        if (template != null) {
          addParentTypes(sb, template);
        }
      }
    }
    sb.append("\n");
  }

  private String getFrameTagName(Element element) {
    String tagName = element.tagName();
    if (tagName.endsWith("Texture")) {
      return "Texture";
    } else if ("ButtonText".equals(tagName)) {
      return "FontString";
    } else if ("ModelFXX".equals(tagName)) {
      return "Model";
    }
    return tagName;
  }

  private void addParentTypes(StringBuilder sb, Template template) {
    String[] mixins = template.getMixins();
    if (mixins != null) {
      addTypes(sb, mixins);
    }
    String[] inherits = template.getInherits();
    if (inherits != null) {
      for (String inherit : inherits) {
        sb.append("|").append(inherit);
        Template t = templates.get(inherit);
        if (t != null) {
          addParentTypes(sb, t);
        }
      }
    }
  }

  private void addTypes(StringBuilder sb, String[] types) {
    for (String type : types) {
      if (!"".equals(type)) {
        sb.append("|").append(type);
      }
    }
  }

  private boolean ownChildWithPK(Element element, String parentName) {
    // 获取所有有 parentKey 属性的子元素
    Elements elements = element.select("* [parentKey]");
    for (Element el : elements) {
      // 判断是否是 element 的子元素
      if (isChild(el, parentName)) {
        return true;
      }
    }
    return false;
  }

  private boolean isChild(Element element, String parentName) {
    for (Element parent : element.parents()) {
      String pk = parent.attr("parentKey");
      String name = parent.attr("name");
      if ("".equals(name) || !"".equals(pk)) {
        continue;
      }
      return name.equals(parentName);
    }
    return false;
  }

  private boolean isExcludeDir(String dir) {
    return "GlueXML".equals(dir) || "LCDXML".equals(dir);
  }

  private long getFileSize(File file) {
    if (file.isFile()) {
      return file.length();
    } else if (file.isDirectory()) {
      long total = 0;
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isFile()) {
            total += f.length();
          } else if (f.isDirectory() && !isExcludeDir(f.getName())) {
            total += getFileSize(f);
          }
        }
      }
      return total;
    }
    return 0;
  }

  private void insertTab(StringBuilder sb, int num) {
    for (int i = 0; i < num; i++) {
      sb.append("    ");
    }
  }

  private String getCodePath() {
    return PropUtil.getProperty(PropUtil.CODE_PATH);
  }
}

class Template {
  private String[] inherits;
  private String[] mixins;
  private Elements childFrames;

  public String[] getInherits() {
    return inherits;
  }

  public void setInherits(String[] inherits) {
    this.inherits = inherits;
  }

  public String[] getMixins() {
    return mixins;
  }

  public void setMixins(String[] mixins) {
    this.mixins = mixins;
  }

  public Elements getChildFrames() {
    return childFrames;
  }

  public void setChildFrames(Elements childFrames) {
    this.childFrames = childFrames;
  }
}
