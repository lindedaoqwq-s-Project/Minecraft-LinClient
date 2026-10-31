package com.lindedaoqwq.linclient.module;

import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.module.setting.Setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all modules: identity, bilingual labels, settings, enable state.
 *
 * Lifecycle rules:
 *  - Modules are registered once at startup and stay subscribed to the {@link
 *    com.lindedaoqwq.linclient.event.EventBus}; handlers of disabled modules are
 *    skipped centrally, so subclasses never need {@code if (!isEnabled()) return;}.
 *  - {@link #setEnabled} is exception-isolated: a broken onEnable/onDisable logs and
 *    still flips the flag, one broken module never takes down the client.
 */
public abstract class Module {
    public final String id;
    public final Category category;
    public final String name, nameZh, desc, descZh;

    private final List<Setting<?>> settings = new ArrayList<Setting<?>>();
    private boolean enabled;

    protected Module(String id, Category cat, String name, String nameZh,
                     String desc, String descZh) {
        this.id = id;
        this.category = cat;
        this.name = name;
        this.nameZh = nameZh;
        this.desc = desc;
        this.descZh = descZh;
    }

    // ---- labels ----

    public String label(boolean zh) { return zh ? nameZh : name; }

    public String desc(boolean zh) { return zh ? descZh : desc; }

    // ---- lifecycle hooks (override as needed; all default to no-op) ----

    /** Called after the module switches on (e.g. remember the gamma to restore). */
    protected void onEnable() { }

    /** Called after the module switches off (restore anything onEnable changed). */
    protected void onDisable() { }

    // ---- state ----

    public final boolean isEnabled() { return enabled; }

    public final void setEnabled(boolean on) {
        if (enabled == on) return;
        enabled = on;
        try {
            if (on) onEnable(); else onDisable();
        } catch (Throwable t) {
            com.lindedaoqwq.linclient.LinClient.LOGGER.warn("Module {} enable/disable hook failed", id, t);
        }
        ConfigManager.markDirty();
    }

    public final void toggle() { setEnabled(!enabled); }

    /** Default switch state for a fresh install (everything stays user-togglable). */
    public boolean defaultEnabled() { return false; }

    // ---- settings ----

    protected void addSettings(Setting<?>... s) {
        for (Setting<?> x : s) {
            if (!settings.contains(x)) settings.add(x);
        }
    }

    public final List<Setting<?>> settings() { return settings; }

    public final Setting<?> setting(String id) {
        for (Setting<?> s : settings) {
            if (s.id.equals(id)) return s;
        }
        return null;
    }

    // ---- persistence (called by ModuleManager) ----

    /** Load switch + settings from config. Config keys: modules.&lt;id&gt;.enabled and
     *  modules.&lt;id&gt;.settings.&lt;settingId&gt;. */
    public final void load() {
        enabled = ConfigManager.getBool("modules." + id + ".enabled", defaultEnabled());
        for (Setting<?> s : settings) s.load("modules." + id + ".settings");
    }

    /** Write switch + settings into the config store (does not save to disk). */
    public final void save() {
        ConfigManager.set("modules." + id + ".enabled", enabled);
        for (Setting<?> s : settings) s.save("modules." + id + ".settings");
    }
}
