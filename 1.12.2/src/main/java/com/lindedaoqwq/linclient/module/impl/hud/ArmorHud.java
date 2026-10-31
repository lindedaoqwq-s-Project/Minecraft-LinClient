package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.module.setting.ModeSetting;
import com.lindedaoqwq.linclient.module.setting.BooleanSetting;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

/** Worn armour pieces with durability, horizontal or vertical. */
public class ArmorHud extends HudModule {
    public final ModeSetting layout = new ModeSetting("layout",
            "\u5e03\u5c40", "Layout", 0, "Horizontal", "Vertical")
            .modesZh(new String[]{"\u6a2a\u5411", "\u7eb5\u5411"});
    public final BooleanSetting durability = new BooleanSetting("durability",
            "\u663e\u793a\u8010\u4e45", "Show durability", true);

    public ArmorHud() {
        super("armorhud", Category.HUD, "Armor HUD", "\u88c5\u5907\u663e\u793a",
                "Worn armour with durability", "\u663e\u793a\u8eab\u4e0a\u88c5\u5907\u4e0e\u8010\u4e45\u5ea6",
                Anchor.RIGHT_BOTTOM, Trait.BACKGROUND);
        addSettings(layout, durability);
    }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        EntityPlayerSP p = mc.player;
        if (p == null) {
            if (editMode) {
                // Empty slot frames as the preview.
                for (int i = 0; i < 4; i++) {
                    int sx = x + (horizontal() ? i * 18 : 0);
                    int sy = y + (horizontal() ? 0 : i * 18);
                    RenderUtils.drawRectOutline(sx, sy, 16, 16, 0x40FFFFFF);
                }
            }
            return;
        }
        ItemStack[] armor = currentArmor(mc);
        boolean horizontal = horizontal();
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        for (int i = 0; i < 4; i++) {
            ItemStack stack = armor[i];
            if (stack == null || stack.isEmpty()) continue;
            int sx = x + (horizontal ? i * 18 : 0);
            int sy = y + (horizontal ? 0 : i * 18);
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, sx, sy);
            if (durability.get()) {
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, sx, sy);
            }
        }
        RenderHelper.disableStandardItemLighting();
    }

    private boolean horizontal() { return layout.get() == 0; }

    /** Armour in display order: helmet, chestplate, leggings, boots. */
    private ItemStack[] currentArmor(Minecraft mc) {
        // 1.8.9: getCurrentArmor()[0] = boots ... [3] = helmet -> reverse for display.
        net.minecraft.util.NonNullList<ItemStack> raw = mc.player.inventory.armorInventory;
        ItemStack[] out = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            out[i] = raw != null && i < raw.size() ? raw.get(3 - i) : null;
        }
        return out;
    }

    @Override
    public int width(Minecraft mc) { return horizontal() ? 4 * 18 - 2 : 16; }

    @Override
    public int height(Minecraft mc) { return horizontal() ? 16 : 4 * 18 - 2; }
}
