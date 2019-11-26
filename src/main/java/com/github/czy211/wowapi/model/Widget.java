package com.github.czy211.wowapi.model;

import java.util.ArrayList;

public class Widget {
    private String name;
    private ArrayList<String> scriptTypes = new ArrayList<>();

    public Widget(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getScriptTypes() {
        return scriptTypes;
    }

    public void setScriptTypes(ArrayList<String> scriptTypes) {
        this.scriptTypes = scriptTypes;
    }
}
