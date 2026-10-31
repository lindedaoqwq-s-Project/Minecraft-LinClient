package com.lindedaoqwq.linclient.module.setting;

import com.lindedaoqwq.linclient.config.ConfigManager;

/**
 * One configurable value of a module. Persisted as
 * {@code modules.<moduleId>.settings.<settingId>} via {@link ConfigManager}.
 *
 * @param <T> value type
 */
public abstract class Setting<T> {
    /** Visibility condition, evaluated when the GUI draws the settings grid. */
    public interface Visible {
        boolean show();
    }

    public final String id;
    public final String label;
    public final String labelZh;
    protected final T defaultValue;
    protected T value;
    private Visible when;

    protected Setting(String id, String label, String labelZh, T defaultValue) {
        this.id = id;
        this.label = label;
        this.labelZh = labelZh;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    /** Chain a visibility condition (e.g. a sub-option only when a toggle is on). */
    @SuppressWarnings("unchecked")
    public final <S extends Setting<T>> S visibleWhen(Visible when) {
        this.when = when;
        return (S) this;
    }

    public final boolean visible() { return when == null || when.show(); }

    public final T get() { return value; }

    public final void set(T v) {
        if (v == null ? value == null : v.equals(value)) return;
        value = v;
        ConfigManager.markDirty();
    }

    public final void reset() { set(defaultValue); }

    public String text(boolean zh) { return zh ? labelZh : label; }

    /** Restore the persisted value (default when absent). */
    public abstract void load(String prefix);

    /** Store the current value (no disk IO). */
    public abstract void save(String prefix);
}
