package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/** Compass direction (N / NE / E / ...). */
public class DirectionHud extends HudModule {
    public DirectionHud() {
        super("direction", Category.HUD, "Direction", "\u65b9\u4f4d",
                "Compass direction", "\u7f57\u76d8\u65b9\u4f4d\u663e\u793a",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    private String line(Minecraft mc, boolean edit) {
        EntityPlayerSP p = mc.player;
        return orPreview(edit, p == null ? null
                : "\u00A7bDir: \u00A7f" + dirOf(p.rotationYaw));
    }

    private static String dirOf(float yaw) {
        yaw = ((yaw % 360F) + 360F) % 360F;
        String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return dirs[(int) ((yaw + 22.5F) / 45F) & 7];
    }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        String s = line(mc, editMode);
        if (s != null) drawText(mc, s, x, y);
    }

    @Override
    public int width(Minecraft mc) { return textWidth(mc, line(mc, false)); }

    @Override
    public int height(Minecraft mc) { return 11; }
}
