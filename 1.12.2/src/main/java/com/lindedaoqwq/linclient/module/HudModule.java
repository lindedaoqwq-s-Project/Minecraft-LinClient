package com.lindedaoqwq.linclient.module;

import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.module.setting.BooleanSetting;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Base for HUD information modules: a small self-drawing element on screen.
 *
 * Layout model:
 *  - Each module declares a default {@link Anchor} plus pixel offsets; {@link
 *    com.lindedaoqwq.linclient.hud.HudManager} turns that into concrete coordinates.
 *  - A user-dragged position is stored as screen fractions (0..1) in
 *    {@code hud.<id>.x / hud.<id>.y}, so layouts survive resolution & GUI-scale changes.
 *  - {@link Trait} flags declare shared appearance capabilities. Common settings for
 *    those (e.g. the background box) are registered here once, so subclasses only
 *    declare their own options.
 */
public abstract class HudModule extends Module {
    /** Appearance capability flags. */
    public enum Trait {
        /** Can draw a rounded background box behind the text. */
        BACKGROUND
    }

    /** Default screen anchor. */
    public enum Anchor { LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM }

    private final Set<Trait> traits;
    private final Anchor anchor;
    private BooleanSetting background;

    protected HudModule(String id, Category cat, String name, String nameZh,
                        String desc, String descZh, Anchor anchor, Trait... traits) {
        super(id, cat, name, nameZh, desc, descZh);
        this.anchor = anchor;
        this.traits = new HashSet<Trait>(Arrays.asList(traits));
        if (this.traits.contains(Trait.BACKGROUND)) {
            background = new BooleanSetting("background",
                    "\u80cc\u666f", "Background", false);
            addSettings(background);
        }
    }

    public boolean hasTrait(Trait t) { return traits.contains(t); }

    public Anchor anchor() { return anchor; }

    /** Whether the shared background box should be drawn. */
    public boolean drawBackground() {
        return background != null && background.get();
    }

    // ---- default layout (used when the user never dragged this element) ----

    /** Horizontal offset in GUI pixels from the anchor edge. */
    public int defaultOffsetX() { return 8; }

    /** Vertical offset in GUI pixels from the anchor edge (top or bottom). */
    public int defaultOffsetY() { return 10; }

    // ---- user position ----

    /** Custom position as screen fractions {fx, fy}, or null when never dragged. */
    public float[] customPos() {
        float fx = ConfigManager.getNum("hud." + id + ".x", -1F);
        float fy = ConfigManager.getNum("hud." + id + ".y", -1F);
        if (fx < 0 || fy < 0 || fx > 1.001F || fy > 1.001F) return null;
        return new float[]{fx, fy};
    }

    public void storeCustomPos(float fx, float fy) {
        ConfigManager.set("hud." + id + ".x", fx);
        ConfigManager.set("hud." + id + ".y", fy);
        ConfigManager.markDirty();
    }

    // ---- drawing helpers ----

    /** Live text, or a bilingual "[Name preview]" placeholder in HUD edit mode. */
    protected String orPreview(boolean edit, String live) {
        if (live != null) return live;
        if (edit) return "[" + label(ConfigManager.isZh()) + " preview]";
        return null;
    }

    protected static void drawText(Minecraft mc, String s, int x, int y) {
        mc.fontRenderer.drawStringWithShadow(s, x, y, 0xFFFFFF);
    }

    protected static int textWidth(Minecraft mc, String s) {
        return mc.fontRenderer.getStringWidth(s) + 2;
    }

    /** Draw the module content with its top-left corner at (x, y). */
    public abstract void render(Minecraft mc, int x, int y, boolean editMode);

    /** Current content width in GUI pixels (used for hit tests and right-alignment). */
    public abstract int width(Minecraft mc);

    /** Current content height in GUI pixels. */
    public abstract int height(Minecraft mc);
}
