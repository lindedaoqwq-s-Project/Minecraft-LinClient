package com.lindedaoqwq.linclient.module.impl.combat;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventAttack;
import com.lindedaoqwq.linclient.event.events.EventWorldChange;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

/** Distance of your last attack. */
public class ReachHud extends HudModule {
    public final NumberSetting time = new NumberSetting("time",
            "\u663e\u793a\u65f6\u957f(\u79d2)", "Show time (s)", 2F, 1F, 5F, 1F, 0);

    private float reach = 0;
    private long expire = 0;

    public ReachHud() {
        super("reach", Category.COMBAT, "Reach Display", "\u653b\u51fb\u8ddd\u79bb",
                "Last attack distance", "\u663e\u793a\u4e0a\u6b21\u653b\u51fb\u7684\u8ddd\u79bb",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
        addSettings(time);
    }

    @Subscribe
    public void onAttack(EventAttack e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !(e.target instanceof EntityLivingBase)) return;
        reach = (float) mc.thePlayer.getDistanceToEntity(e.target);
        expire = System.currentTimeMillis() + (long) (time.get() * 1000F);
    }

    @Subscribe
    public void onWorldChange(EventWorldChange e) {
        reach = 0;
        expire = 0;
    }

    private String line(boolean edit) {
        if (reach > 0 && System.currentTimeMillis() < expire) {
            return orPreview(edit, String.format("\u00A7bReach: \u00A7f%.2fm", reach));
        }
        return orPreview(edit, null);
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
