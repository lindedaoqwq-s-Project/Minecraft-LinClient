package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/** Player position XYZ. */
public class CoordinatesHud extends HudModule {
    public CoordinatesHud() {
        super("coordinates", Category.HUD, "Coordinates", "\u5750\u6807",
                "Position XYZ", "\u5750\u6807 XYZ",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    private String line(Minecraft mc, boolean edit) {
        EntityPlayerSP p = mc.player;
        return orPreview(edit, p == null ? null
                : "\u00A7bXYZ: \u00A7f" + (int) p.posX + " " + (int) p.posY + " " + (int) p.posZ);
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
