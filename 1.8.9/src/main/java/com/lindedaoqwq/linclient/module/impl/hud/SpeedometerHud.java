package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import com.lindedaoqwq.linclient.state.ClientState;
import net.minecraft.client.Minecraft;

/** Horizontal movement speed in blocks per second, with a display multiplier. */
public class SpeedometerHud extends HudModule {
    public final NumberSetting scale = new NumberSetting("scale",
            "\u500d\u7387", "Scale", 1F, 1F, 5F, 1F, 0);

    public SpeedometerHud() {
        super("speedometer", Category.HUD, "Speedometer", "\u901f\u5ea6",
                "Horizontal speed (blocks/s)", "\u6c34\u5e73\u79fb\u52a8\u901f\u5ea6(\u683c/\u79d2)",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
        addSettings(scale);
    }

    private String line(boolean edit) {
        double v = ClientState.speed * Math.max(1F, scale.get());
        return orPreview(edit, String.format("\u00A7bSpeed: \u00A7f%.2f b/s", v));
    }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        String s = line(editMode);
        if (s != null) drawText(mc, s, x, y);
    }

    @Override
    public int width(Minecraft mc) { return textWidth(mc, line(false)); }

    @Override
    public int height(Minecraft mc) { return 11; }
}
