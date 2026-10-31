package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.config.KeyBindings;
import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventCamera;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/** Look around without turning the player: while the key is held the camera stays
 *  frozen at the grab point and the view switches to third person; on release the
 *  camera snaps back to the (still actual) player orientation. */
public class FreeLook extends Module {
    private boolean active = false;
    private boolean forcedThirdPerson = false;
    private float yaw = 0F, pitch = 0F;

    public FreeLook() {
        super("freelook", Category.RENDER, "Free Look", "\u81ea\u7531\u89c6\u89d2",
                "Look around without turning your character", "\u4e0d\u6539\u53d8\u671d\u5411\u81ea\u7531\u89c2\u5bdf\u5468\u56f4");
    }

    @Override
    protected void onDisable() {
        restore(Minecraft.getMinecraft());
    }

    @Subscribe
    public void onTick(EventTick e) {
        Minecraft mc = e.mc;
        EntityPlayerSP p = mc.player;
        boolean held = KeyBindings.FREELOOK.isKeyDown() && p != null && mc.currentScreen == null;
        if (held && !active) {
            active = true;
            yaw = p.rotationYaw;
            pitch = p.rotationPitch;
            if (mc.gameSettings.thirdPersonView == 0) {
                mc.gameSettings.thirdPersonView = 1;
                forcedThirdPerson = true;
            }
        } else if (!held && active) {
            restore(mc);
        }
    }

    private void restore(Minecraft mc) {
        if (forcedThirdPerson) {
            mc.gameSettings.thirdPersonView = 0;
            forcedThirdPerson = false;
        }
        active = false;
    }

    @Subscribe
    public void onCamera(EventCamera e) {
        if (!active) return;
        e.yaw = yaw;
        e.pitch = pitch;
        e.roll = 0F;
    }
}
