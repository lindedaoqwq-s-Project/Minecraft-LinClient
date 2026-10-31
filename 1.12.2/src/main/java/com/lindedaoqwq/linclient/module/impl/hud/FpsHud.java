package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.state.ClientState;
import net.minecraft.client.Minecraft;

/** Live frames per second. */
public class FpsHud extends HudModule {
    public FpsHud() {
        super("fps", Category.HUD, "FPS", "\u5e27\u7387",
                "Live frames per second", "\u5b9e\u65f6\u5e27\u7387",
                Anchor.LEFT_TOP, Trait.BACKGROUND);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        String s = orPreview(editMode, "\u00A7bFPS: \u00A7f" + ClientState.fps);
        if (s != null) drawText(mc, s, x, y);
    }

    @Override
    public int width(Minecraft mc) { return textWidth(mc, "\u00A7bFPS: \u00A7f" + ClientState.fps); }

    @Override
    public int height(Minecraft mc) { return 11; }
}
