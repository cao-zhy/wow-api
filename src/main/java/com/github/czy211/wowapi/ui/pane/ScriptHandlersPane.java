package com.github.czy211.wowapi.ui.pane;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ScriptHandlersPane extends WikiPane {
    private static final String name = "Script_Handlers";
    private static final String extension = ".txt";
    private static final String url = BASE_URI + "/wiki/Widget_script_handlers";
    private static final ScriptHandlersPane pane = new ScriptHandlersPane();
    
    private ScriptHandlersPane() {
        super(name, extension, url);
    }
    
    public static ScriptHandlersPane getInstance() {
        return pane;
    }
    
    @Override
    public void download() {
        StringBuilder result = new StringBuilder();
        Elements elements = connect("span.mw-headline:not(#Widget_API,#Widget_hierarchy,#Example,#References),"
                + "dd:has(a[title^=UIHANDLER ]:eq(0))");
        for (Element element : elements) {
            if (Thread.currentThread().isInterrupted()) {
                canceled();
                return;
            }
            String text = element.text();
            if ("span".equals(element.tagName())) {
                if (getCurrent() > 1) {
                    result.append("}\n");
                }
                result.append(text).append(" = {\n");
            } else {
                String link = getLink("(", element, ")");
                result.append("    ").append(text).append(link).append("\n");
            }
            increaseCurrent(1);
        }
        result.append("}\n");
        createFile(result);
    }
    
    @Override
    public String getRemoteVersion() throws IOException {
        return getPageDateTime();
    }
}
