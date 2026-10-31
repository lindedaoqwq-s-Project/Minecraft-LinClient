package com.lindedaoqwq.linclient.module.setting;

import com.lindedaoqwq.linclient.config.ConfigManager;

/** ARGB colour setting (alpha preserved). */
public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String id, String label, String labelZh, int argbDef) {
        super(id, label, labelZh, argbDef);
    }

    public void load(String prefix) {
        value = ConfigManager.getColor(prefix + "." + id, defaultValue);
    }

    public void save(String prefix) {
        ConfigManager.set(prefix + "." + id, value);
    }
}
