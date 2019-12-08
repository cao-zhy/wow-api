package com.github.czy211.wowapi.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18n {
    public static String getText(String code, Object... arguments) {
        ResourceBundle bundle = ResourceBundle.getBundle("locale");
        String text = bundle.getString(code);
        if (arguments.length > 0) {
            return MessageFormat.format(text, arguments);
        }
        return text;
    }
}
