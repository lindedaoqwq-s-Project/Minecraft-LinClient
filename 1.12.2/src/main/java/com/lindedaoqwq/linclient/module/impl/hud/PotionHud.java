package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Active potion effects with remaining time, drawn as a right-aligned column. */
public class PotionHud extends HudModule {
    public PotionHud() {
        super("potion", Category.PLAYER, "Potion Effects", "\u836f\u6c34\u6548\u679c",
                "Active potion effects & timers", "\u663e\u793a\u8eab\u4e0a\u7684\u836f\u6c34\u6548\u679c\u4e0e\u5269\u4f59\u65f6\u95f4",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
    }

    @Override
    public boolean defaultEnabled() { return true; }

    private List<String> lines(Minecraft mc, boolean edit) {
        EntityPlayerSP p = mc.player;
        List<String> out = new ArrayList<String>();
        if (p != null) {
            Collection<PotionEffect> effects = p.getActivePotionEffects();
            for (PotionEffect e : effects) {
                String name = I18n.format(e.getEffectName());
                if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
                out.add("\u00A75" + name + " \u00A7f" + (e.getDuration() / 20) + "s");
            }
        }
        if (out.isEmpty() && edit) out.add(orPreview(true, null));
        return out;
    }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        List<String> ls = lines(mc, editMode);
        for (String s : ls) {
            int w = textWidth(mc, s);
            drawText(mc, s, x + width(mc) - w, y);   // right-aligned within the box
            y += 11;
        }
    }

    @Override
    public int width(Minecraft mc) {
        int w = 0;
        for (String s : lines(mc, false)) w = Math.max(w, textWidth(mc, s));
        return w;
    }

    @Override
    public int height(Minecraft mc) { return 11 * Math.max(1, lines(mc, false).size()); }
}
