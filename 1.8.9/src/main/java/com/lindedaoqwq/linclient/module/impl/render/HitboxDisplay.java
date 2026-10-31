package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventRender3D;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.setting.ColorSetting;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

/** Draws entity collision boxes in world space. */
public class HitboxDisplay extends Module {
    public final ColorSetting color = new ColorSetting("color",
            "\u989c\u8272", "Colour", 0xFF3ADF6E);
    public final NumberSetting expand = new NumberSetting("expand",
            "\u6269\u5927\u78b0\u649e\u7bb1", "Expand hitbox", 0.1F, 0F, 0.5F, 0.05F, 2);

    public HitboxDisplay() {
        super("hitboxdisplay", Category.RENDER, "Hitbox Display", "\u78b0\u649e\u7bb1\u663e\u793a",
                "Show entity collision boxes", "\u5728\u4e16\u754c\u4e2d\u663e\u793a\u5b9e\u4f53\u78b0\u649e\u7bb1");
        addSettings(color, expand);
    }

    @Subscribe
    public void onRender3D(EventRender3D e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.thePlayer;
        if (mc.theWorld == null || p == null) return;
        double camX = p.lastTickPosX + (p.posX - p.lastTickPosX) * e.partialTicks;
        double camY = p.lastTickPosY + (p.posY - p.lastTickPosY) * e.partialTicks;
        double camZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * e.partialTicks;
        float ex = (float) (double) expand.get();
        GL11.glPushMatrix();
        GL11.glTranslated(-camX, -camY, -camZ);
        for (Object o : mc.theWorld.loadedEntityList) {
            Entity entity = (Entity) o;
            if (!(entity instanceof EntityLivingBase) || entity.isDead || entity == p) continue;
            if (entity.getDistanceSq(p.posX, p.posY, p.posZ) > 64 * 64) continue;
            AxisAlignedBB box = entity.getEntityBoundingBox().expand(ex, ex, ex);
            BlockOverlay.drawBox((float) box.minX, (float) box.minY, (float) box.minZ,
                    (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                    color.get(), 1.5F, 0.85F);
        }
        GL11.glPopMatrix();
    }
}
