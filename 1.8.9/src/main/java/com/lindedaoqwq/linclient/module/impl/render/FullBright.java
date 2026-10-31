package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import net.minecraft.client.Minecraft;

/** Full brightness: pushes gamma to max while on, restores it on disable. */
public class FullBright extends Module {
    private float originalGamma = 0.5F;
    private boolean savedGamma = false;

    public FullBright() {
        super("fullbright", Category.RENDER, "Fullbright", "\u5168\u4eae",
                "See in the dark", "\u70b9\u4eae\u9ed1\u6697\u89d2\u843d\uff0c\u65e0\u9700\u706b\u628a");
    }

    @Override
    public boolean defaultEnabled() { return true; }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!savedGamma) {
            originalGamma = mc.gameSettings.gammaSetting;
            savedGamma = true;
        }
    }

    @Override
    protected void onDisable() {
        if (savedGamma) {
            Minecraft.getMinecraft().gameSettings.gammaSetting = originalGamma;
            savedGamma = false;
        }
    }

    @Subscribe
    public void onTick(EventTick e) {
        e.mc.gameSettings.gammaSetting = 1.0F;
    }
}
