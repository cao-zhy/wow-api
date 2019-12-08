package com.github.czy211.wowapi.model;

import java.io.IOException;

public abstract class APIPage {
    private String name;

    public APIPage(String name) {
        this.name = name;
    }

    public abstract void download() throws IOException;

    public String getName() {
        return name;
    }
}
