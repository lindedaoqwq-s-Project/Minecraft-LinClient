package com.lindedaoqwq.linclient.module;

import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Module registry. Owns the single list of module instances, registers each on the
 * {@link EventBus} once at startup, and bridges config load/save to every module.
 */
public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<Module>();
    private static boolean registered = false;

    private ModuleManager() { }

    /** Instantiate every module once. Called from the mod's init.
     *  Registration order defines the default HUD stacking order per anchor. */
    public static synchronized void registerAll() {
        if (registered) return;
        registered = true;
        // HUD - left column
        register(new com.lindedaoqwq.linclient.module.impl.hud.FpsHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.CpsHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.PingHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.MemoryHud());
        // HUD - right column
        register(new com.lindedaoqwq.linclient.module.impl.hud.CoordinatesHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.DirectionHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.SpeedometerHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.PlaytimeHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.ServerAddressHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.TimeDisplayHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.TntTimerHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.PotionHud());
        register(new com.lindedaoqwq.linclient.module.impl.combat.ComboHud());
        register(new com.lindedaoqwq.linclient.module.impl.combat.ReachHud());
        // HUD - bottom row
        register(new com.lindedaoqwq.linclient.module.impl.hud.KeystrokesHud());
        register(new com.lindedaoqwq.linclient.module.impl.hud.ArmorHud());
        // combat
        register(new com.lindedaoqwq.linclient.module.impl.combat.SprintReset());
        // movement
        register(new com.lindedaoqwq.linclient.module.impl.movement.ToggleSprint());
        register(new com.lindedaoqwq.linclient.module.impl.movement.ToggleSneak());
        // render
        register(new com.lindedaoqwq.linclient.module.impl.render.FullBright());
        register(new com.lindedaoqwq.linclient.module.impl.render.Crosshair());
        register(new com.lindedaoqwq.linclient.module.impl.render.BlockOverlay());
        register(new com.lindedaoqwq.linclient.module.impl.render.HitColor());
        register(new com.lindedaoqwq.linclient.module.impl.render.Zoom());
        register(new com.lindedaoqwq.linclient.module.impl.render.CustomFov());
        register(new com.lindedaoqwq.linclient.module.impl.render.FreeLook());
        register(new com.lindedaoqwq.linclient.module.impl.render.HitboxDisplay());
        register(new com.lindedaoqwq.linclient.module.impl.render.NoHurtCam());
    }

    /** Register one module and subscribe it to the event bus (stay-registered model). */
    static void register(Module m) {
        MODULES.add(m);
        EventBus.register(m);
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static Module byId(String id) {
        for (Module m : MODULES) {
            if (m.id.equals(id)) return m;
        }
        return null;
    }

    /** Typed lookup, e.g. {@code get(FullBright.class).isEnabled()}. */
    public static <T extends Module> T get(Class<T> type) {
        for (Module m : MODULES) {
            if (type.isInstance(m)) return type.cast(m);
        }
        return null;
    }

    public static List<Module> byCategory(Category cat) {
        List<Module> out = new ArrayList<Module>();
        for (Module m : MODULES) {
            if (m.category == cat) out.add(m);
        }
        return out;
    }

    /** Pull every module's state from the config store (after ConfigManager.load). */
    public static void applyConfig() {
        for (Module m : MODULES) m.load();
    }

    /** Push every module's state into the config store and save to disk. */
    public static void persistConfig() {
        for (Module m : MODULES) m.save();
        ConfigManager.saveNow();
    }
}
