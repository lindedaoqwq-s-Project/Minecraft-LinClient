package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventRender2D;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.setting.ColorSetting;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;

/** Custom crosshair drawn over the vanilla one (vanilla is suppressed by ForgeBridge). */
public class Crosshair extends Module {
    public final ColorSetting color = new ColorSetting("color",
            "\u989c\u8272", "Colour", 0xFF3AA6F0);
    public final NumberSetting size = new NumberSetting("size",
            "\u51c6\u661f\u5927\u5c0f", "Crosshair size", 4F, 2F, 8F, 1F, 0);

    public Crosshair() {
        super("crosshair", Category.RENDER, "Custom Crosshair", "\u81ea\u5b9a\u4e49\u51c6\u661f",
                "Custom crosshair, colour & size", "\u66ff\u6362\u51c6\u661f\uff0c\u53ef\u8c03\u989c\u8272\u5927\u5c0f");
        addSettings(color, size);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    @Subscribe
    public void onRender2D(EventRender2D e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) return;
        int cx = mc.displayWidth / 2, cy = mc.displayHeight / 2;
        // Convert to GUI coordinates through the current scale factor.
        int f = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        int x = cx / f, y = cy / f;
        int s = size.get().intValue();
        int c = color.get();
        int outline = 0xB3000000 | (c & 0x00FFFFFF);
        // 1px black outline, then the coloured cross.
        RenderUtils.drawRect(x - s - 1, y - 1, 2 * s + 2, 2, outline);
        RenderUtils.drawRect(x - 1, y - s - 1, 2, 2 * s + 2, outline);
        RenderUtils.drawRect(x - s, y, 2 * s, 1, c);
        RenderUtils.drawRect(x, y - s, 1, 2 * s, c);
    }
}
