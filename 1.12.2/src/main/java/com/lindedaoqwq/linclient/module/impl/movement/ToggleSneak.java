package com.lindedaoqwq.linclient.module.impl.movement;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/** Press-to-toggle sneaking: a tap of the sneak key latches sneaking on/off instead
 *  of having to hold it. */
public class ToggleSneak extends Module {
    private boolean sneaking = false;
    private boolean wasDown = false;

    public ToggleSneak() {
        super("togglesneak", Category.MOVEMENT, "Toggle Sneak", "\u5207\u6362\u6f5c\u884c",
                "Tap sneak to toggle it on/off", "\u70b9\u6309\u6f5c\u884c\u952e\u5207\u6362\u6f5c\u884c\u72b6\u6001");
    }

    @Subscribe
    public void onTick(EventTick e) {
        Minecraft mc = e.mc;
        if (mc.player == null) {
            sneaking = false;
            wasDown = false;
            return;
        }
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        boolean down = sneak.isKeyDown();
        if (down && !wasDown) sneaking = !sneaking;   // fresh press flips the latch
        wasDown = down;
        KeyBinding.setKeyBindState(sneak.getKeyCode(), sneaking);
    }
}
