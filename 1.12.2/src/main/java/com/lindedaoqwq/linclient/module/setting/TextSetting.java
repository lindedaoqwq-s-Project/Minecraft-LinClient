package com.lindedaoqwq.linclient.module.setting;

import com.lindedaoqwq.linclient.config.ConfigManager;

/** Free-text setting (single line). */
public final class TextSetting extends Setting<String> {
    public TextSetting(String id, String label, String labelZh, String def) {
        super(id, label, labelZh, def);
    }

    public void load(String prefix) {
        value = ConfigManager.getStr(prefix + "." + id, defaultValue);
    }

    public void save(String prefix) {
        ConfigManager.set(prefix + "." + id, value);
    }
}
