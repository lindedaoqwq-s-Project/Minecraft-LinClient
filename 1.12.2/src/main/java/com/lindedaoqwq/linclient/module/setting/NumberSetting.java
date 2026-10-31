package com.lindedaoqwq.linclient.module.setting;

import com.lindedaoqwq.linclient.config.ConfigManager;

/** Numeric slider setting with range, step and display decimals. */
public final class NumberSetting extends Setting<Float> {
    public final float min, max, step;
    public final int decimals;

    public NumberSetting(String id, String label, String labelZh,
                         float def, float min, float max, float step, int decimals) {
        super(id, label, labelZh, def);
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = decimals;
    }

    /** True when the value should render as an integer (unit steps across the range). */
    public boolean isIntegral() { return step >= 1F && min % 1F == 0F && max % 1F == 0F; }

    public String format(float v) {
        if (isIntegral()) return String.valueOf((int) v);
        return String.format("%." + decimals + "f", v);
    }

    /** Clamp + quantise a raw slider position to a legal value. */
    public float quantize(float raw) {
        float v = Math.round(raw / step) * step;
        return Math.max(min, Math.min(max, v));
    }

    public void load(String prefix) {
        value = ConfigManager.getNum(prefix + "." + id, defaultValue);
    }

    public void save(String prefix) {
        ConfigManager.set(prefix + "." + id, value);
    }
}
