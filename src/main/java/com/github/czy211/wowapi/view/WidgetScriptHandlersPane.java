package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.constant.LinkConst;
import com.github.czy211.wowapi.constant.WidgetConst;
import com.github.czy211.wowapi.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WidgetScriptHandlersPane extends BaseApiPane {
    private static final String API_URL = LinkConst.WIKI_BASE + "/Widget_script_handlers";
    public static final String[] SCRIPT_OBJECT_FUNCTIONS = {"GetScript", "SetScript", "HookScript", "HasScript"};
    public HashMap<String, ArrayList<String>> scriptHandler = new HashMap<>();
    public HashSet<String> scriptTypes = new HashSet<>();

    public WidgetScriptHandlersPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            Document document = Jsoup.connect(API_URL).get();
            connectSuccess();

            Elements elements = document.select("span.mw-headline:not(#Widget_API,#Widget_hierarchy,#Example,"
                    + "#References),dd:has(a[title^=UIHANDLER ]:eq(0))");
            int total = elements.size();
            int current = 0;
            String widgetName = "";
            for (Element element : elements) {
                current++;
                updateProgress((double) current / total);

                String text = element.text();
                if ("span".equals(element.tagName())) {
                    if (current > 1) {
                        appendWidgetScriptHandlers(sb, widgetName);
                    }
                    widgetName = text;
                    scriptHandler.put(widgetName, new ArrayList<>());
                } else {
                    String scriptType = element.selectFirst("a").text();
                    scriptHandler.get(widgetName).add(scriptType);
                    scriptTypes.add(scriptType);
                }
                sb.append("--- ").append(text).append("\n");
            }
            if (sb.length() > 0) {
                appendWidgetScriptHandlers(sb, widgetName);
                sb.append("---@alias ScriptType string ");
                for (String scriptType : scriptTypes) {
                    sb.append("|'\"").append(scriptType).append("\"'");
                }
                try (PrintWriter writer = new PrintWriter(Utils.getDownloadPath() + getName(), "UTF-8")) {
                    writer.println(EnumVersionType.PREFIX + getRemoteVersion());
                    writer.println();
                    writer.print(WidgetConst.WIDGET_HIERARCHY);
                    writer.println(sb);
                }
            }
        } catch (IOException e) {
            throw new IOException(API_URL, e);
        }
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return Utils.getRemoteTimestamp(API_URL);
    }

    public void appendWidgetScriptHandlers(StringBuilder sb, String widgetName) {
        sb.append("---@alias ").append(widgetName).append("ScriptType string ");
        for (String scriptType : scriptHandler.get(widgetName)) {
            sb.append("|'\"").append(scriptType).append("\"'");
        }
        sb.append("\n\n");
        for (String funcName : SCRIPT_OBJECT_FUNCTIONS) {
            sb.append("---@param scriptType ");
            if (!"HasScript".equals(funcName)) {
                if ("AnimationGroup".equals(widgetName) || "Animation".equals(widgetName)
                        || "Frame".equals(widgetName)) {
                    sb.append("ScriptObjectScriptType|");
                }
                sb.append(widgetName);
            }
            sb.append("ScriptType\nfunction ").append(widgetName).append(":").append(funcName)
                    .append("(scriptType, ...) end\n\n");
        }
    }
}
