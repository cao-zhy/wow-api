package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WidgetScriptTypesPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_script_handlers/Complete_list";

    public WidgetScriptTypesPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    public static void appendScriptTypes(ArrayList<String> scriptTypes, StringBuilder sb, String name) {
        if (scriptTypes.size() > 0) {
            sb.append("---@alias ").append(name).append("ScriptType string ");
            for (String scriptType : scriptTypes) {
                sb.append(scriptType);
            }
            scriptTypes.clear();

            if (!"".equals(name)) {
                sb.append("\n\n---@param scriptType ").append(name).append("ScriptType\n")
                        .append("---@param handler function\n")
                        .append("function ").append(name).append(":SetScript(scriptType, handler) end\n\n")
                        .append("---@param scriptType ").append(name).append("ScriptType\n")
                        .append("---@param handler function\n")
                        .append("function ").append(name).append(":HookScript(scriptType, handler) end\n\n")
                        .append("---@param scriptType ").append(name).append("ScriptType\n")
                        .append("function ").append(name).append(":GetScript(scriptType) end\n\n");
            }
        }
    }

    @Override
    public void download() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            Document document = Jsoup.connect(API_URL).get();
            connectSuccess();

            Elements elements = document.select("span.mw-headline,dd:has(a[title^=UIHANDLER ]:eq(0))");
            int total = elements.size();
            int current = 0;
            String name = "";
            ArrayList<String> scriptTypes = new ArrayList<>();
            for (Element element : elements) {
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                if ("span".equals(element.tagName())) {
                    // 标签名是 “span”，则匹配到 widget 名称，添加之前保存到列表的 scriptType
                    appendScriptTypes(scriptTypes, sb, name);
                    // 更新 widget 名
                    name = text;
                } else {
                    scriptTypes.add("| '\"" + text + "\"' ");
                }
            }
            appendScriptTypes(scriptTypes, sb, name);

            try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                writer.println(EnumVersionType.PREFIX + getRemoteVersion());
                writer.println();
                writer.println(sb);
            }
        } catch (IOException e) {
            throw new IOException(API_URL, e);
        }
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return Utils.getRemoteTimestamp(API_URL);
    }
}
