package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;

import java.util.List;

/** Fuse countdown of the primed TNT nearest to the player. */
public class TntTimerHud extends HudModule {
    public TntTimerHud() {
        super("tnttimer", Category.HUD, "TNT Timer", "TNT \u8ba1\u65f6",
                "Countdown of the nearest lit TNT", "\u6700\u8fd1\u70b9\u71c3 TNT \u7684\u7206\u70b8\u5012\u8ba1\u65f6",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
    }

    /** Fuse of the nearest primed TNT in seconds, or -1 when none is around. */
    private double nearestFuse(Minecraft mc) {
        if (mc.theWorld == null || mc.thePlayer == null) return -1;
        List<Entity> entities = mc.theWorld.loadedEntityList;
        EntityTNTPrimed best = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < entities.size(); i++) {
            Entity e = (Entity) entities.get(i);
            if (!(e instanceof EntityTNTPrimed) || e.isDead) continue;
            double d = e.getDistanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            if (d < bestDist) {
                bestDist = d;
                best = (EntityTNTPrimed) e;
            }
        }
        if (best == null || bestDist > 32 * 32) return -1;
        return best.fuse / 20.0;
    }

    private String line(Minecraft mc, boolean edit) {
        double fuse = nearestFuse(mc);
        if (fuse < 0) return null;
        String c = fuse <= 1.0 ? "\u00A7c" : (fuse <= 2.5 ? "\u00A76" : "\u00A7b");
        return c + "TNT: \u00A7f" + String.format("%.1fs", fuse);
    }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        String s = orPreview(editMode, line(mc, false));
        if (s != null) drawText(mc, s, x, y);
    }

    @Override
    public int width(Minecraft mc) { return textWidth(mc, orPreview(false, line(mc, false))); }

    @Override
    public int height(Minecraft mc) { return 11; }
}
