package com.lindedaoqwq.linclient.hud;

import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.ModuleManager;
import com.lindedaoqwq.linclient.state.ClientState;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HUD render pipeline. Places every {@link HudModule} and draws it:
 *  - a user-dragged position (screen fractions) always wins;
 *  - otherwise modules stack from their declared anchor in registration order.
 * Exposes {@link #BOUNDS} (GUI-pixel rectangles) for the HUD editor hit tests.
 */
public final class HudManager {
    /** Rendered rectangle per module id: {x, y, w, h}, refreshed every frame. */
    public static final Map<String, int[]> BOUNDS = new HashMap<String, int[]>();

    private static final int GAP = 2;
    private static final int BG = 0x8A101018;

    private HudManager() { }

    /** All registered HUD modules in registration (stacking) order. */
    public static List<HudModule> hudModules() {
        List<HudModule> out = new ArrayList<HudModule>();
        for (com.lindedaoqwq.linclient.module.Module m : ModuleManager.all()) {
            if (m instanceof HudModule) out.add((HudModule) m);
        }
        return out;
    }

    /** Draw all (edit mode) or all enabled (game) HUD modules. */
    public static void render(Minecraft mc, boolean editMode) {
        if (!editMode) {
            if (!ClientState.modActive || !ClientState.hudEnabled) return;
            if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();
        BOUNDS.clear();

        int yLT = 10, yRT = 10;                       // top anchors stack downward
        int yLB = sh - 10, yRB = sh - 10;             // bottom anchors stack upward

        for (HudModule h : hudModules()) {
            if (!editMode && !h.isEnabled()) continue;
            int w = Math.max(1, h.width(mc));
            int ht = Math.max(1, h.height(mc));
            int x, y;
            float[] custom = h.customPos();
            if (custom != null) {
                x = clamp((int) (custom[0] * sw), 0, sw - 20);
                y = clamp((int) (custom[1] * sh), 0, sh - 12);
            } else {
                switch (h.anchor()) {
                    case LEFT_TOP:
                        x = h.defaultOffsetX();
                        y = yLT;
                        yLT += ht + GAP;
                        break;
                    case RIGHT_TOP:
                        x = sw - h.defaultOffsetX() - w;
                        y = yRT;
                        yRT += ht + GAP;
                        break;
                    case LEFT_BOTTOM:
                        x = h.defaultOffsetX();
                        y = yLB - ht;
                        yLB = y - GAP;
                        break;
                    case RIGHT_BOTTOM:
                    default:
                        x = sw - h.defaultOffsetX() - w;
                        y = yRB - ht;
                        yRB = y - GAP;
                        break;
                }
                x = clamp(x, 0, sw - w);
                y = clamp(y, 0, sh - ht);
            }
            if (h.drawBackground()) {
                RenderUtils.drawRounded(x - 3, y - 2, w + 6, ht + 4, BG, 3);
            }
            h.render(mc, x, y, editMode);
            BOUNDS.put(h.id, new int[]{x, y, w, ht});
        }
    }

    private static int clamp(int v, int lo, int hi) { return v < lo ? lo : (v > hi ? hi : v); }
}
