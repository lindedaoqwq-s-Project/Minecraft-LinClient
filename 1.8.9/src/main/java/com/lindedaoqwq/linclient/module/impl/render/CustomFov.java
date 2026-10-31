package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventFov;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.setting.BooleanSetting;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;

/** Fixed custom FOV, optionally without the speed-based FOV change. */
public class CustomFov extends Module {
    public final NumberSetting fov = new NumberSetting("fov",
            "\u76ee\u6807 FOV", "Target FOV", 90F, 30F, 110F, 1F, 0);
    public final BooleanSetting noSpeedFov = new BooleanSetting("noSpeedFov",
            "\u53bb\u9664\u901f\u5ea6\u53d8\u7126", "No speed FOV change", false);

    private float originalFov = -1F;

    public CustomFov() {
        super("customfov", Category.RENDER, "Custom FOV", "\u81ea\u5b9a\u4e49\u89c6\u91ce",
                "Fixed FOV with optional speed-zoom removal", "\u56fa\u5b9a\u89c6\u91ce\uff0c\u53ef\u53bb\u9664\u79fb\u52a8\u53d8\u7126");
        addSettings(fov, noSpeedFov);
    }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (originalFov < 0) originalFov = mc.gameSettings.fovSetting;
    }

    @Override
    protected void onDisable() {
        if (originalFov > 0) {
            Minecraft.getMinecraft().gameSettings.fovSetting = originalFov;
            originalFov = -1F;
        }
    }

    @Subscribe
    public void onTick(EventTick e) {
        e.mc.gameSettings.fovSetting = fov.get();
    }

    @Subscribe
    public void onFov(EventFov e) {
        if (noSpeedFov.get()) e.setCanceled(true);
    }
}
