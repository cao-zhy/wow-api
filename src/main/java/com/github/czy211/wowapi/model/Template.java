package com.github.czy211.wowapi.model;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;

public class Template {
    private String name;
    private ArrayList<String> interfaces = new ArrayList<>();
    private HashMap<String, Element> widgets = new HashMap<>();

    public Template(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(ArrayList<String> interfaces) {
        this.interfaces = interfaces;
    }

    public HashMap<String, Element> getWidgets() {
        return widgets;
    }

    public void setWidgets(HashMap<String, Element> widgets) {
        this.widgets = widgets;
    }
}
