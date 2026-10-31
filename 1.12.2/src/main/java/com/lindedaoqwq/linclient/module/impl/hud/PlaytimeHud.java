package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import net.minecraft.client.Minecraft;

/** Session play time (HH:mm:ss since client start). */
public class PlaytimeHud extends HudModule {
    public PlaytimeHud() {
        super("playtime", Category.HUD, "Playtime", "\u6e38\u620f\u65f6\u957f",
                "Session play time", "\u672c\u6b21\u4f1a\u8bdd\u6e38\u620f\u65f6\u957f",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
    }

    private String line(boolean edit) {
        long sec = Math.max(0L,
                (System.currentTimeMillis() - com.lindedaoqwq.linclient.state.ClientState.sessionStart) / 1000L);
        long h = sec / 3600L, m = (sec % 3600L) / 60L, s = sec % 60L;
        return orPreview(edit, String.format("\u00A7bTime: \u00A7f%02d:%02d:%02d", h, m, s));
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
