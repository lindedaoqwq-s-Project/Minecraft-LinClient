package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventHurt;
import com.lindedaoqwq.linclient.event.events.EventRender2D;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.setting.ColorSetting;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/** Screen-edge flash when you take damage (pure 2D overlay, no hurt-cam changes). */
public class HitColor extends Module {
    public final ColorSetting color = new ColorSetting("color",
            "\u989c\u8272", "Colour", 0xFFE02F2F);
    public final NumberSetting strength = new NumberSetting("strength",
            "\u5f3a\u5ea6", "Strength", 45F, 10F, 100F, 5F, 0);

    private static final long DURATION_MS = 450L;
    private long lastHit = 0L;

    public HitColor() {
        super("hitcolor", Category.RENDER, "Hit Colour", "\u53d7\u51fb\u67d3\u8272",
                "Screen-edge flash when you take damage", "\u53d7\u51fb\u65f6\u5c4f\u5e55\u8fb9\u7f18\u6cdb\u7ea2");
        addSettings(color, strength);
    }

    @Subscribe
    public void onHurt(EventHurt e) {
        lastHit = System.currentTimeMillis();
    }

    @Subscribe
    public void onRender2D(EventRender2D e) {
        long elapsed = System.currentTimeMillis() - lastHit;
        if (lastHit == 0L || elapsed > DURATION_MS) return;
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        float decay = 1F - elapsed / (float) DURATION_MS;
        int alpha = (int) (strength.get() / 100F * decay * 255F) & 0xFF;
        // Four edge bands, each fading inwards.
        int band = Math.max(12, h / 12);
        for (int i = 0; i < band; i++) {
            float t = 1F - (float) i / band;
            int a = (int) (alpha * t) & 0xFF;
            int col = (a << 24) | (color.get() & 0x00FFFFFF);
            RenderUtils.drawRect(0, i, w, 1, col);                  // top
            RenderUtils.drawRect(0, h - 1 - i, w, 1, col);          // bottom
            RenderUtils.drawRect(i, 0, 1, h, col);                  // left
            RenderUtils.drawRect(w - 1 - i, 0, 1, h, col);          // right
        }
    }
}
