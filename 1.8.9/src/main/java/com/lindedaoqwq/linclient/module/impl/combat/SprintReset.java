package com.lindedaoqwq.linclient.module.impl.combat;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventAttack;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/** W-tap helper: releases sprint for one tick after an attack so the next
 *  sprint press restarts momentum (vanilla combat mechanics, client-side keys only). */
public class SprintReset extends Module {
    private boolean pending = false;

    public SprintReset() {
        super("sprintreset", Category.COMBAT, "Sprint Reset", "\u75be\u8dd1\u91cd\u7f6e",
                "Reset sprint on attack (W-tap helper)", "\u653b\u51fb\u540e\u91cd\u7f6e\u75be\u8dd1\u72b6\u6001");
    }

    @Subscribe
    public void onAttack(EventAttack e) {
        pending = true;
    }

    @Subscribe
    public void onTick(EventTick e) {
        if (!pending) return;
        pending = false;
        Minecraft mc = e.mc;
        if (mc.thePlayer == null) return;
        KeyBinding sprint = mc.gameSettings.keyBindSprint;
        KeyBinding.setKeyBindState(sprint.getKeyCode(), false);
    }
}
