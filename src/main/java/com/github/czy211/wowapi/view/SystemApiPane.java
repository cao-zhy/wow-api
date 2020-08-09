package com.github.czy211.wowapi.view;

import com.github.czy211.wowapi.constant.EnumVersionType;
import com.github.czy211.wowapi.util.Utils;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class SystemApiPane extends BaseApiPane {
    public SystemApiPane(String name, EnumVersionType versionType) {
        super(name, versionType);
        HBox rightNode = (HBox) getRight();
        rightNode.getChildren().remove(getBtDownload());
    }

    @Override
    public void download() {

    }

    @Override
    public long getRemoteVersion() throws IOException {
        return Utils.getBuild()[1];
    }
}
