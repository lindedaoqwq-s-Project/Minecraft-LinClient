package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;

/** Used / max heap memory, with a colour warning threshold. */
public class MemoryHud extends HudModule {
    public final NumberSetting warn = new NumberSetting("warn",
            "\u8b66\u793a\u9608\u503c(%)", "Warn at (%)", 85F, 50F, 95F, 5F, 0);

    public MemoryHud() {
        super("memory", Category.HUD, "Memory", "\u5185\u5b58",
                "Used / max heap memory", "\u5df2\u7528\u4e0e\u6700\u5927\u5806\u5185\u5b58",
                Anchor.LEFT_TOP, Trait.BACKGROUND);
        addSettings(warn);
    }

    private String line(boolean edit) {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        int pct = max > 0 ? (int) (used * 100L / max) : 0;
        String c = pct >= warn.get() ? "\u00A7c" : "\u00A7b";
        return orPreview(edit, c + "Mem: \u00A7f" + used + "/" + max + "MB");
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
