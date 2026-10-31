package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.ColorSetting;
import com.lindedaoqwq.linclient.state.ClientState;
import com.lindedaoqwq.linclient.gui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;

/** WASD + mouse button display. */
public class KeystrokesHud extends HudModule {
    public final ColorSetting color = new ColorSetting("color",
            "\u9ad8\u4eae\u989c\u8272", "Highlight colour", 0xFF3AA6F0);

    public KeystrokesHud() {
        super("keystrokes", Category.HUD, "Keystrokes", "\u6309\u952e\u663e\u793a",
                "WASD & mouse buttons display", "\u663e\u793a WASD \u4e0e\u9f20\u6807\u6309\u952e\u72b6\u6001",
                Anchor.LEFT_BOTTOM, Trait.BACKGROUND);
        addSettings(color);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    private static final int B = 20, G = 3;

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        int hi = color.get();
        GameSettings gs = mc.gameSettings;
        boolean[] s = {
                gs.keyBindForward.isKeyDown(),
                gs.keyBindLeft.isKeyDown(),
                gs.keyBindBack.isKeyDown(),
                gs.keyBindRight.isKeyDown()
        };
        String[] l = {"W", "A", "S", "D"};
        int[][] pos = {{1, 0}, {0, 1}, {1, 1}, {2, 1}};
        for (int i = 0; i < 4; i++) {
            int kx = x + pos[i][0] * (B + G), ky = y + pos[i][1] * (B + G);
            Theme.roundRect(kx, ky, B, B, s[i] ? hi : 0xB3141420);
            drawText(mc, l[i], kx + (B - mc.fontRendererObj.getStringWidth(l[i])) / 2,
                    ky + 6, s[i] ? 0x9FD8FF : 0xFFFFFF);
        }
        int my = y + 2 * (B + G);
        String[] ml = {"LMB " + ClientState.leftCps(), "RMB " + ClientState.rightCps()};
        int[] mw = {B * 2 + G, B};
        int mx = x;
        for (int i = 0; i < 2; i++) {
            boolean held = i == 0 ? ClientState.leftHeld : ClientState.rightHeld;
            Theme.roundRect(mx, my, mw[i], B, held ? hi : 0xB3141420);
            drawText(mc, ml[i], mx + 3, my + 6, 0xFFFFFF);
            mx += mw[i] + G;
        }
    }

    private static void drawText(Minecraft mc, String s, int x, int y, int color) {
        mc.fontRendererObj.drawStringWithShadow(s, x, y, color);
    }

    @Override
    public int width(Minecraft mc) { return B * 3 + G * 2; }

    @Override
    public int height(Minecraft mc) { return B * 3 + G * 2; }
}
