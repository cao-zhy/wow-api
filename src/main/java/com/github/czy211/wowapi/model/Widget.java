package com.github.czy211.wowapi.model;

import java.util.ArrayList;

public class Widget {
    private String name;
    private ArrayList<String> handlers = new ArrayList<>();

    public Widget(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("    ").append(name).append(" = {\n");
        for (String handler : handlers) {
            result.append("        \"").append(handler).append("\",\n");
        }
        result.append("    },\n");
        return result.toString();
    }

    public ArrayList<String> getHandlers() {
        return handlers;
    }
}
