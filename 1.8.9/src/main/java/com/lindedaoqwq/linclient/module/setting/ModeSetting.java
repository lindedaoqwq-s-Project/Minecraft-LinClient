package com.lindedaoqwq.linclient.module.setting;

import com.lindedaoqwq.linclient.config.ConfigManager;

/** Mode picker: one of a fixed list of options, stored by index. */
public final class ModeSetting extends Setting<Integer> {
    public final String[] modes;
    private String[] modesZh;

    public ModeSetting(String id, String label, String labelZh, int defIndex, String... modes) {
        super(id, label, labelZh, defIndex);
        this.modes = modes;
        this.modesZh = modes;   // same strings by default; override via modesZh()
    }

    /** Bilingual display names for the modes (same length as modes). */
    public ModeSetting modesZh(String... zh) {
        if (zh.length == modes.length) this.modesZh = zh;
        return this;
    }

    public int count() { return modes.length; }

    public int next() { return (value + 1) % modes.length; }

    public String text(int index, boolean zh) {
        if (index < 0 || index >= modes.length) return "";
        return zh ? modesZh[index] : modes[index];
    }

    public void load(String prefix) {
        int v = (int) ConfigManager.getNum(prefix + "." + id, defaultValue);
        value = Math.max(0, Math.min(modes.length - 1, v));
    }

    public void save(String prefix) {
        ConfigManager.set(prefix + "." + id, (float) value);
    }
}
