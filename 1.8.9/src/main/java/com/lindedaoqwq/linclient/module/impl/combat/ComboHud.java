package com.lindedaoqwq.linclient.module.impl.combat;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventAttack;
import com.lindedaoqwq.linclient.event.events.EventHurt;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.event.events.EventWorldChange;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/** Counts consecutive hits on living targets; resets when you get hurt. */
public class ComboHud extends HudModule {
    public final NumberSetting time = new NumberSetting("time",
            "\u663e\u793a\u65f6\u957f(\u79d2)", "Show time (s)", 2F, 1F, 5F, 1F, 0);

    private int combo = 0;
    private long expire = 0;
    private Entity pendingTarget = null;
    private float pendingHealth = -1F;

    public ComboHud() {
        super("combo", Category.COMBAT, "Combo Counter", "\u8fde\u51fb\u8ba1\u6570",
                "Counts consecutive hits", "\u7edf\u8ba1\u8fde\u7eed\u547d\u4e2d\u6b21\u6570",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
        addSettings(time);
    }

    @Subscribe
    public void onAttack(EventAttack e) {
        if (!(e.target instanceof EntityLivingBase)) return;
        pendingTarget = e.target;
        pendingHealth = ((EntityLivingBase) e.target).getHealth();
    }

    @Subscribe
    public void onHurt(EventHurt e) {
        combo = 0;
        expire = 0;
    }

    @Subscribe
    public void onWorldChange(EventWorldChange e) {
        combo = 0;
        expire = 0;
        pendingTarget = null;
    }

    @Subscribe
    public void onTick(EventTick e) {
        long now = System.currentTimeMillis();
        if (combo > 0 && now > expire) {
            combo = 0;
            expire = 0;
        }
        if (pendingTarget == null) return;
        if (!(pendingTarget instanceof EntityLivingBase) || pendingTarget.isDead) {
            pendingTarget = null;
            return;
        }
        float nowHealth = ((EntityLivingBase) pendingTarget).getHealth();
        if (nowHealth < pendingHealth - 0.01F) {
            combo++;
            expire = now + (long) (time.get() * 1000F);
            pendingTarget = null;
        } else if (nowHealth > pendingHealth) {
            pendingHealth = nowHealth;   // healed in between; keep waiting
        }
    }

    private String line(boolean edit) {
        if (combo > 0 && System.currentTimeMillis() < expire) {
            return orPreview(edit, "\u00A76Combo: \u00A7f" + combo);
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
