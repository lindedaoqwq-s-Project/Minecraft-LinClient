package com.lindedaoqwq.linclient.module.impl.render;

import com.lindedaoqwq.linclient.event.Subscribe;
import com.lindedaoqwq.linclient.event.events.EventBlockOutline;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.setting.ColorSetting;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

/** Custom block selection outline (replaces the vanilla thin black box). */
public class BlockOverlay extends Module {
    public final ColorSetting color = new ColorSetting("color",
            "\u63cf\u8fb9\u989c\u8272", "Outline colour", 0xFF3BA9F0);
    public final NumberSetting width = new NumberSetting("width",
            "\u63cf\u8fb9\u5bbd\u5ea6", "Outline width", 2F, 1F, 5F, 1F, 0);

    public BlockOverlay() {
        super("blockoverlay", Category.RENDER, "Block Overlay", "\u65b9\u5757\u63cf\u8fb9",
                "Custom block selection outline", "\u81ea\u5b9a\u4e49\u65b9\u5757\u9009\u4e2d\u63cf\u8fb9");
        addSettings(color, width);
    }

    @Subscribe
    public void onBlockOutline(EventBlockOutline e) {
        MovingObjectPosition mop = e.target;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || mop.getBlockPos() == null) return;
        e.setCanceled(true);   // suppress the vanilla outline
        drawBox(mop.getBlockPos().getX() - 0.005F, mop.getBlockPos().getY() - 0.005F,
                mop.getBlockPos().getZ() - 0.005F,
                mop.getBlockPos().getX() + 1.005F, mop.getBlockPos().getY() + 1.005F,
                mop.getBlockPos().getZ() + 1.005F,
                color.get(), Math.max(1F, width.get()), 0.9F);
    }

    /** Shared wireframe box (also used by the hitbox display). Camera-relative translation
     *  must be done by the caller. */
    static void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                        int argb, float lineWidth, float alpha) {
        float r = ((argb >> 16) & 0xFF) / 255F;
        float g = ((argb >> 8) & 0xFF) / 255F;
        float b = (argb & 0xFF) / 255F;
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(r, g, b, alpha);
        GL11.glBegin(GL11.GL_LINES);
        // bottom rectangle
        GL11.glVertex3f(x1, y1, z1); GL11.glVertex3f(x2, y1, z1);
        GL11.glVertex3f(x2, y1, z1); GL11.glVertex3f(x2, y1, z2);
        GL11.glVertex3f(x2, y1, z2); GL11.glVertex3f(x1, y1, z2);
        GL11.glVertex3f(x1, y1, z2); GL11.glVertex3f(x1, y1, z1);
        // top rectangle
        GL11.glVertex3f(x1, y2, z1); GL11.glVertex3f(x2, y2, z1);
        GL11.glVertex3f(x2, y2, z1); GL11.glVertex3f(x2, y2, z2);
        GL11.glVertex3f(x2, y2, z2); GL11.glVertex3f(x1, y2, z2);
        GL11.glVertex3f(x1, y2, z2); GL11.glVertex3f(x1, y2, z1);
        // verticals
        GL11.glVertex3f(x1, y1, z1); GL11.glVertex3f(x1, y2, z1);
        GL11.glVertex3f(x2, y1, z1); GL11.glVertex3f(x2, y2, z1);
        GL11.glVertex3f(x2, y1, z2); GL11.glVertex3f(x2, y2, z2);
        GL11.glVertex3f(x1, y1, z2); GL11.glVertex3f(x1, y2, z2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopMatrix();
    }
}
