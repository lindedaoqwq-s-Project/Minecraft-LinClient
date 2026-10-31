package com.lindedaoqwq.linclient.module.impl.movement;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;

/** Sprint automatically while moving forward. */
public class ToggleSprint extends Module {
    public ToggleSprint() {
        super("togglesprint", Category.MOVEMENT, "Toggle Sprint", "\u81ea\u52a8\u75be\u8dd1",
                "Sprint automatically when moving", "\u524d\u8fdb\u65f6\u81ea\u52a8\u75be\u8dd1");
    }

    @Override
    public boolean defaultEnabled() { return true; }

    @Subscribe
    public void onTick(EventTick e) {
        Minecraft mc = e.mc;
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        KeyBinding sprint = mc.gameSettings.keyBindSprint;
        boolean want = mc.currentScreen == null
                && mc.gameSettings.keyBindForward.isKeyDown()
                && !p.isSneaking() && !p.capabilities.isFlying;
        KeyBinding.setKeyBindState(sprint.getKeyCode(), want);
    }
}
