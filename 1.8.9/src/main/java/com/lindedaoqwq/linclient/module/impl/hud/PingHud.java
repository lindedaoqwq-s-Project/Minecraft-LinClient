package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.state.ClientState;
import net.minecraft.client.Minecraft;

/** Current server latency in milliseconds. */
public class PingHud extends HudModule {
    public PingHud() {
        super("ping", Category.HUD, "Ping", "\u5ef6\u8fdf",
                "Current server latency (ms)", "\u5f53\u524d\u670d\u52a1\u5668\u5ef6\u8fdf(ms)",
                Anchor.LEFT_TOP, Trait.BACKGROUND);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    private String line(boolean edit) {
        return orPreview(edit, "\u00A7bPing: \u00A7f" + ClientState.ping + "ms");
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
