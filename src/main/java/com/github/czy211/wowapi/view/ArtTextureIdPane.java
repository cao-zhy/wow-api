package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;

import java.io.IOException;

public class ArtTextureIdPane extends BaseApiPane {
    private static final String FILEPATH = "/Helix/ArtTextureID.lua";

    public ArtTextureIdPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        downloadFxmlFile(FILEPATH);
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return getRemoteBuild(FILEPATH);
    }
}
