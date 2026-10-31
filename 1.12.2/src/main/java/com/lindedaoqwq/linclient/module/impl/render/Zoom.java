package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.config.KeyBindings;
import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;

/** Hold-to-zoom via the vanilla FOV setting: no reflection, no events, works on both
 *  versions. The original FOV is restored on key release AND on module disable. */
public class Zoom extends Module {
    public final NumberSetting level = new NumberSetting("level",
            "\u7f29\u653e\u500d\u7387", "Zoom level", 5F, 2F, 10F, 1F, 0);

    private boolean active = false;
    private float originalFov = -1F;

    public Zoom() {
        super("zoom", Category.RENDER, "Zoom", "\u5feb\u901f\u7f29\u653e",
                "Hold a key to zoom in", "\u6309\u4f4f\u5feb\u6377\u952e\u7f29\u5c0f\u89c6\u91ce");
        addSettings(level);
    }

    @Override
    protected void onDisable() {
        restore(Minecraft.getMinecraft());
    }

    @Subscribe
    public void onTick(EventTick e) {
        Minecraft mc = e.mc;
        boolean held = KeyBindings.ZOOM.isKeyDown() && mc.currentScreen == null;
        if (held && !active) {
            originalFov = mc.gameSettings.fovSetting;
            float zoom = Math.max(1.5F, level.get());
            mc.gameSettings.fovSetting = originalFov / zoom;
            active = true;
        } else if (!held && active) {
            restore(mc);
        }
    }

    private void restore(Minecraft mc) {
        if (active && originalFov > 0) {
            mc.gameSettings.fovSetting = originalFov;
        }
        active = false;
        originalFov = -1F;
    }
}
