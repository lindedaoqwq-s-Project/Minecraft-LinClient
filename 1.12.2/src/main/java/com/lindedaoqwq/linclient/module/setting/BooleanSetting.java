package com.lindedaoqwq.linclient.module.setting;

import com.lindedaoqwq.linclient.config.ConfigManager;

/** On/off setting. */
public final class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String id, String label, String labelZh, boolean def) {
        super(id, label, labelZh, def);
    }

    public void load(String prefix) {
        value = ConfigManager.getBool(prefix + "." + id, defaultValue);
    }

    public void save(String prefix) {
        ConfigManager.set(prefix + "." + id, value);
    }
}
