package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.util.Utils;

import java.io.IOException;

public class AtlasInfoPane extends BaseApiPane {
    private static final String FILEPATH = "/Helix/AtlasInfo.lua";

    public AtlasInfoPane(String name, EnumVersionType versionType) {
        super(name, versionType);
    }

    @Override
    public void download() throws IOException {
        downloadFxmlFile(FILEPATH);
    }

    @Override
    public long getRemoteVersion() throws IOException {
        return Utils.getRemoteBuild(FILEPATH);
    }
}
