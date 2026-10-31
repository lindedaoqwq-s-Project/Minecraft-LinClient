package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.state.ClientState;
import net.minecraft.client.Minecraft;

/** Left / right clicks per second. */
public class CpsHud extends HudModule {
    public CpsHud() {
        super("cps", Category.HUD, "CPS", "\u70b9\u51fb\u901f\u5ea6",
                "Left / right clicks per second", "\u5de6\u53f3\u952e\u6bcf\u79d2\u70b9\u51fb\u6570",
                Anchor.LEFT_TOP, Trait.BACKGROUND);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    private String line(boolean edit) {
        return orPreview(edit, "\u00A7bCPS: \u00A7f"
                + ClientState.leftCps() + " | " + ClientState.rightCps());
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
