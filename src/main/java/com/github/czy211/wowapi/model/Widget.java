package com.github.czy211.wowapi.model;

import java.util.ArrayList;

public class Widget {
    private String name;
    private ArrayList<String> handlers = new ArrayList<>();

    public Widget(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getHandlers() {
        return handlers;
    }
}
