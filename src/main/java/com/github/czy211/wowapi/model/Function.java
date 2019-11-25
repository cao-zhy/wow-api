package com.github.czy211.wowapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 函数类
 */
class Function {
    /**
     * 不合法参数值和其代替值的映射表
     */
    private static final HashMap<String, String> ARGUMENT_REPLACEMENT = new HashMap<>();
    /**
     * 不合法参数值
     */
    private static final Set<String> KEYS;

    static {
        ARGUMENT_REPLACEMENT.put("end", "stop");
        ARGUMENT_REPLACEMENT.put("function", "func");
        ARGUMENT_REPLACEMENT.put("1", "one");
        ARGUMENT_REPLACEMENT.put("etc.", "...");
        ARGUMENT_REPLACEMENT.put("repeat", "duplicate");
        ARGUMENT_REPLACEMENT.put("..", "...");
        KEYS = ARGUMENT_REPLACEMENT.keySet();
    }

    private String description;
    private String url;
    private String name;
    private ArrayList<String> arguments;

    Function(String description, String url, String name, ArrayList<String> arguments) {
        this.description = description;
        this.url = url;
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("--- ").append(description);
        if (!"".equals(url)) {
            result.append(" [").append(url).append("]");
        }
        result.append("\nfunction ").append(name).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            String arg = arguments.get(i);
            // 有多余的逗号
            if ("".equals(arg)) {
                continue;
            }
            // 使用 {} 包含描述内容
            if ("setting0=normal".equals(arg)) {
                arg = "setting";
                result.append(arg);
                break;
            }
            // 参数值不合法
            if (KEYS.contains(arg)) {
                arg = ARGUMENT_REPLACEMENT.get(arg);
            }
            result.append(arg);
            if (i < arguments.size() - 1) {
                result.append(", ");
            }
        }
        result.append(") end\n");
        return result.toString();
    }
}
