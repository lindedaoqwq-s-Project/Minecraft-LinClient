package com.lindedaoqwq.linclient.hud;

import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Map;

/**
 * HUD layout editor. Drag elements to reposition (positions persist as screen
 * fractions), snap to a fine grid, alignment guides at the screen centre, right-click
 * an element to disable its module, ESC to save & close.
 */
public class HudEditorScreen extends GuiScreen {
    private static final int SNAP = 4;         // grid snap in GUI pixels

    private String dragging = null;
    private int grabDx, grabDy;
    private boolean snapX = false, snapY = false;   // centre alignment guides visible
    private boolean zh;

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        zh = ConfigManager.isZh();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Dim backdrop (no super.drawScreen: works identically in-game and at the main menu).
        RenderUtils.drawRect(0, 0, width, height, 0x60000000);
        // Alignment guides while dragging.
        if (dragging != null) {
            if (snapX) RenderUtils.drawRect(width / 2, 0, 1, height, 0x803ADFDF);
            if (snapY) RenderUtils.drawRect(0, height / 2, width, 1, 0x803ADFDF);
        }
        // Render all modules with drag outlines (placeholders when no live data).
        HudManager.render(mc, true);
        Map<String, int[]> b = HudManager.BOUNDS;
        if (dragging != null && b.containsKey(dragging)) {
            int[] r = b.get(dragging);
            RenderUtils.drawRectOutline(r[0] - 4, r[1] - 4, r[2] + 8, r[3] + 8, 0xFF3ADFDF);
            HudModule h = byId(dragging);
            if (h != null) {
                String name = h.label(zh);
                mc.fontRenderer.drawStringWithShadow(name, r[0], r[1] + r[3] + 8, 0xFF9FD8FF);
            }
        }
        String tip = zh
                ? "\u62d6\u52a8\u79fb\u52a8 \u00b7 \u53f3\u952e\u7981\u7528 \u00b7 ESC \u4fdd\u5b58\u9000\u51fa"
                : "Drag to move \u00b7 Right-click to disable \u00b7 ESC to save & close";
        mc.fontRenderer.drawStringWithShadow(tip, 6, height - 14, 0xFFAAFFAA);
    }

    private HudModule byId(String id) {
        for (HudModule h : HudManager.hudModules()) {
            if (h.id.equals(id)) return h;
        }
        return null;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging == null) {
            // Pick the topmost element whose bounds contain the point (iterate reverse
            // registration order so later-drawn = topmost wins).
            Map<String, int[]> b = HudManager.BOUNDS;
            java.util.List<HudModule> mods = HudManager.hudModules();
            for (int i = mods.size() - 1; i >= 0; i--) {
                String id = mods.get(i).id;
                int[] r = b.get(id);
                if (r == null) continue;
                if (mouseX >= r[0] - 3 && mouseX <= r[0] + r[2] + 3
                        && mouseY >= r[1] - 3 && mouseY <= r[1] + r[3] + 3) {
                    dragging = id;
                    grabDx = mouseX - r[0];
                    grabDy = mouseY - r[1];
                    break;
                }
            }
        }
        if (dragging != null) {
            int px = mouseX - grabDx;
            int py = mouseY - grabDy;
            // Snap to grid; lock onto the screen centre lines.
            px = (px + SNAP / 2) / SNAP * SNAP;
            py = (py + SNAP / 2) / SNAP * SNAP;
            snapX = Math.abs(px + widthOf(dragging) / 2 - width / 2) <= SNAP;
            snapY = Math.abs(py + heightOf(dragging) / 2 - height / 2) <= SNAP;
            if (snapX) px = (width - widthOf(dragging)) / 2;
            if (snapY) py = (height - heightOf(dragging)) / 2;
            HudModule h = byId(dragging);
            if (h != null) {
                h.storeCustomPos(clampF(px / (float) width, 0F, 1F),
                        clampF(py / (float) height, 0F, 1F));
            }
        }
    }

    private int widthOf(String id) {
        int[] r = HudManager.BOUNDS.get(id);
        return r != null ? r[2] : 0;
    }

    private int heightOf(String id) {
        int[] r = HudManager.BOUNDS.get(id);
        return r != null ? r[3] : 0;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1) {   // right-click: disable the module under the cursor
            Map<String, int[]> b = HudManager.BOUNDS;
            java.util.List<HudModule> mods = HudManager.hudModules();
            for (int i = mods.size() - 1; i >= 0; i--) {
                HudModule h = mods.get(i);
                int[] r = b.get(h.id);
                if (r == null) continue;
                if (mouseX >= r[0] - 3 && mouseX <= r[0] + r[2] + 3
                        && mouseY >= r[1] - 3 && mouseY <= r[1] + r[3] + 3) {
                    h.setEnabled(false);
                    return;
                }
            }
        }
    }

    private static float clampF(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = null;
        snapX = false;
        snapY = false;
        ConfigManager.saveNow();
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        super.keyTyped(c, key);
        if (key == 1) {
            ConfigManager.saveNow();
            mc.displayGuiScreen(null);
        }
    }
}
