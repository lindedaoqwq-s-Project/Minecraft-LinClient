package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.BooleanSetting;
import net.minecraft.client.Minecraft;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Real-world clock (24h/12h, optional seconds). */
public class TimeDisplayHud extends HudModule {
    public final BooleanSetting hour24 = new BooleanSetting("hour24",
            "24 \u5c0f\u65f6\u5236", "24-hour clock", true);
    public final BooleanSetting seconds = new BooleanSetting("seconds",
            "\u663e\u793a\u79d2", "Show seconds", false);

    public TimeDisplayHud() {
        super("timedisplay", Category.HUD, "Time Display", "\u65f6\u95f4\u663e\u793a",
                "Real-world clock", "\u73b0\u5b9e\u65f6\u95f4\u65f6\u949f",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
        addSettings(hour24, seconds);
    }

    private String line(boolean edit) {
        String pattern = hour24.get() ? "HH:mm" : "hh:mm a";
        if (seconds.get()) pattern = hour24.get() ? "HH:mm:ss" : "hh:mm:ss a";
        return orPreview(edit, "\u00A7bClock: \u00A7f"
                + new SimpleDateFormat(pattern).format(new Date()));
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
