package com.lindedaoqwq.linclient.gui;

import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.hud.HudEditorScreen;
import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.Module;
import com.lindedaoqwq.linclient.module.ModuleManager;
import com.lindedaoqwq.linclient.module.setting.BooleanSetting;
import com.lindedaoqwq.linclient.module.setting.ColorSetting;
import com.lindedaoqwq.linclient.module.setting.ModeSetting;
import com.lindedaoqwq.linclient.module.setting.NumberSetting;
import com.lindedaoqwq.linclient.module.setting.Setting;
import com.lindedaoqwq.linclient.util.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** LinClient ClickGUI: centred single-window glass panel (structure inspired by modern
 *  client GUIs, not a copy of any one client). Left sidebar: brand, search, category
 *  nav with counts, HUD layout editor entry. Main area: module rows with an iOS-style
 *  toggle and an expand chevron; rows with settings expand in place into a two-column
 *  grid of toggles / sliders / mode pickers / colour dots. Sky-blue accent is kept. */
public class ClickGuiScreen extends GuiScreen {

    private static class Row {      // a module row in the list
        int x, y, w, h;
        Module module;
        Row(int x, int y, int w, int h, Module module) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.module = module;
        }
    }

    private static class Ctrl {     // a control cell inside an expanded settings grid
        int x, y, w, h;
        Setting<?> set;
        Ctrl(int x, int y, int w, int h, Setting<?> set) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.set = set;
        }
    }

    private static final int[] PALETTE = {
            0xFFF2F2F2, 0xFFE04A3A, 0xFFE0912F, 0xFFE0D22F,
            0xFF3ADF6E, 0xFF3ADFE0, 0xFF3AA6F0, 0xFFB03ADF
    };

    private int selected = 0;
    private String search = "";
    private boolean searchFocus = false;
    private String expandedId = null;    // module whose settings grid is open
    private Module colorTarget = null;   // module whose colour popup is open
    private int scrollOff = 0;
    private boolean zh;

    private final List<Row> rows = new ArrayList<Row>();
    private final List<Ctrl> ctrls = new ArrayList<Ctrl>();

    private int px, py, pw, ph;
    private final int sideW = 92;
    private int listTop, listBottom;

    private boolean dragBar = false;
    private int grabBY;
    private Ctrl dragSlider = null;

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        zh = ConfigManager.isZh();
        scrollOff = 0;
    }

    // ---------- layout ----------

    private int panelW() { return Math.min(470, width - 20); }
    private int panelH() { return Math.min(300, height - 20); }

    private int contentX() { return px + sideW + 8; }
    private int contentW() { return px + pw - 8 - contentX(); }

    private List<Setting<?>> visibleSettings(Module m) {
        List<Setting<?>> out = new ArrayList<Setting<?>>();
        for (Setting<?> s : m.settings()) {
            if (s.visible() && usable(s)) out.add(s);
        }
        return out;
    }

    /** Setting types the grid can render (TextSetting is reserved for future use). */
    private static boolean usable(Setting<?> s) {
        return s instanceof BooleanSetting || s instanceof NumberSetting
                || s instanceof ColorSetting || s instanceof ModeSetting;
    }

    private int settingsH(Module m) {
        List<Setting<?>> ss = visibleSettings(m);
        if (ss.isEmpty()) return 0;
        int rows2 = (ss.size() + 1) / 2;
        return 1 + 4 + rows2 * 19 + 3;   // divider + pad + grid + pad
    }

    private int rowHeight(Module m) {
        return m.id.equals(expandedId) ? 22 + settingsH(m) : 22;
    }

    private int contentH() {
        int h = 0;
        for (Module m : visibleModules()) h += rowHeight(m) + 3;
        return h;
    }

    private int maxScroll() { return Math.max(0, contentH() - (listBottom - listTop)); }

    private int catCount(Category cat) { return ModuleManager.byCategory(cat).size(); }

    private int catEnabled(Category cat) {
        int n = 0;
        for (Module m : ModuleManager.byCategory(cat)) {
            if (m.isEnabled()) n++;
        }
        return n;
    }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<Module>();
        String q = search.trim().toLowerCase();
        Category[] cats = Category.values();
        Category sel = cats[Math.min(selected, cats.length - 1)];
        for (Module m : ModuleManager.all()) {
            if (q.isEmpty()) {
                if (m.category == sel) out.add(m);
            } else {
                String hay = (m.name + " " + m.nameZh + " " + m.desc + " " + m.descZh).toLowerCase();
                if (hay.contains(q)) out.add(m);
            }
        }
        return out;
    }

    private void buildRows() {
        rows.clear();
        ctrls.clear();
        int y = listTop - scrollOff;
        int x0 = contentX();
        for (Module m : visibleModules()) {
            int h = rowHeight(m);
            rows.add(new Row(x0, y, contentW(), h, m));
            if (m.id.equals(expandedId)) {
                List<Setting<?>> ss = visibleSettings(m);
                int gy = y + 22 + 1 + 4;
                int colW = (contentW() - 12 - 6) / 2;
                for (int i = 0; i < ss.size(); i++) {
                    int cx = x0 + 6 + (i % 2) * (colW + 6);
                    ctrls.add(new Ctrl(cx, gy + (i / 2) * 19, colW, 19, ss.get(i)));
                }
            }
            y += h + 3;
        }
    }

    private void clampScroll() {
        int max = maxScroll();
        if (scrollOff < 0) scrollOff = 0;
        if (scrollOff > max) scrollOff = max;
    }

    // ---------- drawing ----------

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        px = (width - panelW()) / 2;
        py = (height - panelH()) / 2;
        pw = panelW();
        ph = panelH();
        listTop = py + 30;
        listBottom = py + ph - 8;
        buildRows();
        clampScroll();

        // world veil + glass panel
        RenderUtils.drawRect(0, 0, width, height, 0x94000000);
        RenderUtils.drawRounded(px, py, pw, ph, Theme.STROKE_STRONG, 9);
        RenderUtils.drawRounded(px + 1, py + 1, pw - 2, ph - 2, Theme.GLASS, 8);

        // ---- sidebar ----
        RenderUtils.drawRect(px + sideW, py + 6, 1, ph - 12, Theme.STROKE);
        drawSidebar(mouseX, mouseY);

        // ---- main head ----
        boolean searching = !search.trim().isEmpty();
        Category[] cats = Category.values();
        Category sel = cats[Math.min(selected, cats.length - 1)];
        String title = searching ? (zh ? "\u641c\u7d22" : "Search") : sel.text(zh);
        fontRenderer.drawStringWithShadow(title, contentX(), py + 9, Theme.TEXT);
        String sub = searching
                ? (visibleModules().size() + (zh ? " \u4e2a\u7ed3\u679c" : " results"))
                : (visibleModules().size() + (zh ? " \u4e2a\u6a21\u5757 \u00b7 \u5df2\u542f\u7528 "
                    + catEnabled(sel) + " \u4e2a"
                    : " modules \u00b7 " + catEnabled(sel) + " on"));
        fontRenderer.drawStringWithShadow(sub, contentX() + 10
                + fontRenderer.getStringWidth(title), py + 11, Theme.TEXT_2);

        // ---- module rows (clipped) ----
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int f = new ScaledResolution(mc).getScaleFactor();
        GL11.glScissor(contentX() * f, mc.displayHeight - listBottom * f,
                (px + pw - 8 - contentX()) * f, (listBottom - listTop) * f);
        for (Row r : rows) {
            if (r.y + r.h < listTop - 6 || r.y > listBottom + 6) continue;
            drawModuleRow(r, mouseX, mouseY);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // ---- scrollbar ----
        int content = contentH();
        int view = listBottom - listTop;
        if (content > view) {
            int barX = px + pw - 7;
            int thumbH = Math.max(18, view * view / content);
            int thumbY = listTop + (view - thumbH) * scrollOff / maxScroll();
            RenderUtils.drawRounded(barX, thumbY, 3, thumbH, 0x38FFFFFF, 1);
        }

        // ---- colour popup ----
        if (colorTarget != null) drawColorPopup();
    }

    private void drawSidebar(int mouseX, int mouseY) {
        int sx = px + 7;
        int pad = 7;
        // brand: mark + name + version
        int mX = sx, mY = py + 8;
        RenderUtils.drawRounded(mX, mY, 12, 12, Theme.ACCENT, 4);
        GL11.glPushMatrix();
        GL11.glTranslatef(mX + 3.5F, mY + 2.5F, 0);
        fontRenderer.drawStringWithShadow("L", 0, 0, 0xFFFFFFFF);
        GL11.glPopMatrix();
        fontRenderer.drawStringWithShadow("LinClient", mX + 16, mY + 0, Theme.TEXT);
        fontRenderer.drawStringWithShadow(com.lindedaoqwq.linclient.LinClient.VERSION,
                mX + 16, mY + 8, Theme.TEXT_3);

        // search (capsule)
        int swH = 15;
        int sy = py + 26;
        RenderUtils.drawRounded(sx, sy, sideW - 2 * pad, swH,
                searchFocus ? Theme.ACCENT : Theme.STROKE, swH / 2);
        RenderUtils.drawRounded(sx + 1, sy + 1, sideW - 2 * pad - 2, swH - 2,
                0x40000000, swH / 2 - 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.45F, 0.5F, 0.56F, 1F);
        GL11.glLineWidth(1F);
        GL11.glBegin(GL11.GL_LINES);
        float mcx = sx + 8, mcy = sy + swH / 2F - 1F, mr = 2.6F;
        for (int i = 0; i < 8; i++) {
            float a1 = (float) (i * Math.PI / 4), a2 = (float) ((i + 1) * Math.PI / 4);
            vertex(mcx + mr * (float) Math.cos(a1), mcy + mr * (float) Math.sin(a1),
                    mcx + mr * (float) Math.cos(a2), mcy + mr * (float) Math.sin(a2));
        }
        vertex(mcx + 2, mcy + 2, mcx + 5, mcy + 5);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        String show = search.isEmpty() && !searchFocus
                ? (zh ? "\u641c\u7d22..." : "Search...") : search;
        fontRenderer.drawStringWithShadow(show, sx + 13, sy + 4,
                search.isEmpty() && !searchFocus ? Theme.TEXT_3 : Theme.TEXT);
        if (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0) {
            int tx = sx + 13 + fontRenderer.getStringWidth(search);
            RenderUtils.drawRect(tx + 1, sy + 3, 1, swH - 6, Theme.TEXT);
        }

        // category nav with counts
        Category[] cats = Category.values();
        int ny = py + 48;
        for (int i = 0; i < cats.length; i++) {
            boolean sel = search.trim().isEmpty() && i == selected;
            boolean hover = mouseX >= sx && mouseX <= sx + sideW - 2 * pad
                    && mouseY >= ny && mouseY <= ny + 16;
            if (sel) {
                RenderUtils.drawRounded(sx, ny, sideW - 2 * pad, 16, Theme.ACCENT, 8);
            } else if (hover) {
                RenderUtils.drawRounded(sx, ny, sideW - 2 * pad, 16, Theme.LAYER_HOVER, 8);
            }
            drawIcon(cats[i].name().toLowerCase(), sx + 5, ny + 4, 8,
                    sel ? 0xFFFFFFFF : (hover ? 0xFFD0D4DA : Theme.TEXT_2));
            fontRenderer.drawStringWithShadow(cats[i].text(zh), sx + 16, ny + 4,
                    sel ? 0xFFFFFFFF : (hover ? 0xFFE8EAEE : Theme.TEXT_2));
            String n = String.valueOf(catCount(cats[i]));
            fontRenderer.drawStringWithShadow(n, sx + sideW - 2 * pad - 2
                    - fontRenderer.getStringWidth(n), ny + 4,
                    sel ? 0xCCFFFFFF : Theme.TEXT_3);
            ny += 18;
        }

        // bottom: HUD layout editor
        int hy = py + ph - 24;
        boolean hHover = mouseX >= sx && mouseX <= sx + sideW - 2 * pad
                && mouseY >= hy && mouseY <= hy + 16;
        if (hHover) RenderUtils.drawRounded(sx, hy, sideW - 2 * pad, 16, Theme.LAYER_HOVER, 8);
        drawIcon("hudedit", sx + 5, hy + 4, 8, hHover ? 0xFFD0D4DA : Theme.TEXT_2);
        fontRenderer.drawStringWithShadow(zh ? "HUD \u5e03\u5c40" : "HUD Layout",
                sx + 16, hy + 4, hHover ? 0xFFE8EAEE : Theme.TEXT_2);
    }

    private void drawModuleRow(Row r, int mouseX, int mouseY) {
        Module m = r.module;
        boolean expanded = m.id.equals(expandedId);
        boolean on = m.isEnabled();
        boolean hover = mouseX >= r.x && mouseX <= r.x + r.w && mouseY >= r.y && mouseY <= r.y + 22;
        int h = expanded ? r.h : 22;
        int bg = expanded ? 0x2E000000 : (hover ? Theme.LAYER_HOVER : Theme.LAYER);
        RenderUtils.drawRounded(r.x, r.y, r.w, h, expanded ? Theme.STROKE_STRONG : 0x0AFFFFFF, 5);
        RenderUtils.drawRounded(r.x + 1, r.y + 1, r.w - 2, h - 2, bg, 4);

        // name + inline description
        String name = m.label(zh);
        fontRenderer.drawStringWithShadow(name, r.x + 7, r.y + 7, Theme.TEXT);
        String desc = m.desc(zh);
        int nx = r.x + 7 + fontRenderer.getStringWidth(name) + 6;
        int maxDesc = r.w - 100 - (nx - r.x);
        if (maxDesc > 20) {
            if (fontRenderer.getStringWidth(desc) > maxDesc) {
                desc = fontRenderer.trimStringToWidth(desc, maxDesc - 4) + "...";
            }
            fontRenderer.drawStringWithShadow(desc, nx, r.y + 7,
                    expanded ? Theme.TEXT_2 : Theme.TEXT_3);
        }

        // iOS-style switch (right)
        int swW = 18, swH = 10;
        int swX = r.x + r.w - swW - 16, swY = r.y + 6;
        RenderUtils.drawRounded(swX, swY, swW, swH, on ? Theme.ACCENT : Theme.LAYER_ACTIVE, swH / 2);
        int k = swH - 2;
        int kx = on ? swX + swW - k - 1 : swX + 1;
        RenderUtils.drawRounded(kx, swY + 1, k, k, 0xFFEAECEF, k / 2);

        // chevron: > when collapsed, v when expanded
        int chX = r.x + r.w - 11, chY = r.y + 8;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.55F, 0.55F, 0.58F, 1F);
        GL11.glLineWidth(1.2F);
        GL11.glBegin(GL11.GL_LINES);
        if (expanded) {
            vertex(chX - 2.5F, chY, chX, chY + 3);
            vertex(chX, chY + 3, chX + 2.5F, chY);
        } else {
            vertex(chX, chY - 2.5F, chX + 2.5F, chY);
            vertex(chX + 2.5F, chY, chX, chY + 2.5F);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        // expanded settings grid (two columns)
        if (expanded) {
            int divY = r.y + 22;
            RenderUtils.drawRect(r.x + 4, divY, r.w - 8, 1, Theme.STROKE);
            for (Ctrl c : ctrls) {
                drawCtrl(m, c);
            }
        }
    }

    private void drawCtrl(Module m, Ctrl c) {
        Setting<?> s = c.set;
        if (s instanceof BooleanSetting) {
            boolean v = ((BooleanSetting) s).get();
            fontRenderer.drawStringWithShadow(s.text(zh), c.x, c.y + 1, Theme.TEXT_2);
            RenderUtils.drawRounded(c.x + c.w - 19, c.y, 18, 10,
                    v ? Theme.ACCENT : Theme.LAYER_ACTIVE, 5);
            int k = 8;
            int kx = v ? c.x + c.w - 19 + 18 - k - 1 : c.x + c.w - 19 + 1;
            RenderUtils.drawRounded(kx, c.y + 1, k, k, 0xFFEAECEF, k / 2);
        } else if (s instanceof NumberSetting) {
            NumberSetting n = (NumberSetting) s;
            float v = n.get();
            float frac = Math.max(0F, Math.min(1F, (v - n.min) / (n.max - n.min)));
            fontRenderer.drawStringWithShadow(s.text(zh), c.x, c.y + 1, Theme.TEXT_2);
            String val = n.format(v);
            fontRenderer.drawStringWithShadow(val, c.x + c.w
                    - fontRenderer.getStringWidth(val), c.y + 1, Theme.TEXT_2);
            int tX = c.x, tW = c.w - 26, tY = c.y + 12;
            RenderUtils.drawRounded(tX, tY, tW, 3, Theme.LAYER_ACTIVE, 1);
            RenderUtils.drawRounded(tX, tY, Math.max(2, (int) (tW * frac)), 3, Theme.ACCENT, 1);
            RenderUtils.drawRounded(tX + (int) (tW * frac) - 3, tY - 2, 7, 7, 0xFFEAECEF, 3);
        } else if (s instanceof ModeSetting) {
            ModeSetting mode = (ModeSetting) s;
            fontRenderer.drawStringWithShadow(s.text(zh), c.x, c.y + 1, Theme.TEXT_2);
            String val = "< " + mode.text(mode.get(), zh) + " >";
            fontRenderer.drawStringWithShadow(val, c.x + c.w
                    - fontRenderer.getStringWidth(val), c.y + 1, Theme.TEXT_2);
        } else if (s instanceof ColorSetting) {
            int cur = ((ColorSetting) s).get();
            fontRenderer.drawStringWithShadow(s.text(zh), c.x, c.y + 1, Theme.TEXT_2);
            String hex = String.format("#%06X", cur & 0xFFFFFF);
            fontRenderer.drawStringWithShadow(hex, c.x + c.w - 24
                    - fontRenderer.getStringWidth(hex), c.y + 1, Theme.TEXT_3);
            RenderUtils.drawRounded(c.x + c.w - 9, c.y, 9, 9, 0x40FFFFFF, 4);
            RenderUtils.drawRounded(c.x + c.w - 8, c.y + 1, 7, 7, cur, 3);
        }
    }

    private void drawColorPopup() {
        int def = 0xFF3AA6F0;
        for (Setting<?> s : colorTarget.settings()) {
            if (s instanceof ColorSetting) { def = ((ColorSetting) s).get(); break; }
        }
        ColorSetting target = findColor(colorTarget);
        int cur = target != null ? target.get() : def;
        int pw2 = 190, ph2 = 30 + 4 * 40 + 16;
        int x = (width - pw2) / 2, y = (height - ph2) / 2;
        RenderUtils.drawRect(0, 0, width, height, 0x64000000);
        RenderUtils.drawRounded(x, y, pw2, ph2, Theme.STROKE_STRONG, 9);
        RenderUtils.drawRounded(x + 1, y + 1, pw2 - 2, ph2 - 2, Theme.GLASS, 8);
        String title = zh ? "\u989c\u8272" : "Colour";
        fontRenderer.drawStringWithShadow(title, x + 12, y + 10, Theme.TEXT);
        String hex = String.format("#%06X", cur & 0xFFFFFF);
        fontRenderer.drawStringWithShadow(hex, x + pw2 - 12
                - fontRenderer.getStringWidth(hex), y + 10, Theme.TEXT_2);
        for (int i = 0; i < PALETTE.length; i++) {
            int cx = x + 12 + (i % 4) * 42, cyy = y + 28 + (i / 4) * 40;
            RenderUtils.drawRounded(cx, cyy, 34, 34, PALETTE[i], 7);
        }
        String defS = zh ? "\u9ed8\u8ba4" : "Default";
        int dx = x + 12 + 3 * 42 + 4;
        RenderUtils.drawRounded(dx, y + 28 + 3 * 40, 34, 34, Theme.LAYER, 7);
        RenderUtils.drawRounded(dx + 1, y + 29 + 3 * 40, 32, 32, 0x12000000, 6);
        fontRenderer.drawStringWithShadow(defS, dx, y + 28 + 3 * 40 + 40, Theme.TEXT_3);
    }

    private ColorSetting findColor(Module m) {
        for (Setting<?> s : m.settings()) {
            if (s instanceof ColorSetting) return (ColorSetting) s;
        }
        return null;
    }

    // ---------- vector icons (16 logical px, scalable) ----------

    private void drawIcon(String id, float x, float y, float s, int argb) {
        float r = ((argb >> 16) & 0xFF) / 255F, g = ((argb >> 8) & 0xFF) / 255F,
                b = (argb & 0xFF) / 255F, a = ((argb >> 24) & 0xFF) / 255F;
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(s / 16F, s / 16F, 1F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(2F);
        GL11.glBegin(GL11.GL_LINES);
        if (id.equals("hud")) {                    // monitor / dashboard
            vertex(2, 3, 14, 3);  vertex(14, 3, 14, 11);
            vertex(14, 11, 2, 11); vertex(2, 11, 2, 3);
            vertex(6, 14, 10, 14); vertex(8, 11, 8, 14);
        } else if (id.equals("combat")) {          // crossed swords
            vertex(3, 3, 13, 13); vertex(13, 3, 3, 13);
            vertex(3, 6, 6, 3);   vertex(10, 13, 13, 10);
        } else if (id.equals("movement")) {        // arrow right
            vertex(2, 8, 13, 8);  vertex(8, 3, 13, 8);  vertex(8, 13, 13, 8);
        } else if (id.equals("player")) {          // head + shoulders
            vertex(6, 2, 10, 2);  vertex(10, 2, 10, 6);
            vertex(10, 6, 6, 6);  vertex(6, 6, 6, 2);
            vertex(3, 14, 8, 8);  vertex(8, 8, 13, 14);
        } else if (id.equals("render")) {          // eye
            vertex(8, 3, 14, 8);  vertex(14, 8, 8, 13);
            vertex(8, 13, 2, 8);  vertex(2, 8, 8, 3);
            vertex(7, 7, 9, 9);   vertex(9, 7, 7, 9);
        } else if (id.equals("hudedit")) {         // pencil
            vertex(3, 13, 11, 5);
            vertex(11, 5, 13, 3);
            vertex(2, 14, 4, 12);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopMatrix();
    }

    private void vertex(float x1, float y1, float x2, float y2) {
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
    }

    // ---------- input ----------

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d != 0 && colorTarget == null) {
            scrollOff += d > 0 ? -18 : 18;
            clampScroll();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // colour popup first
        if (colorTarget != null) {
            int pw2 = 190, ph2 = 30 + 4 * 40 + 16;
            int x = (width - pw2) / 2, y = (height - ph2) / 2;
            for (int i = 0; i < PALETTE.length; i++) {
                int cx = x + 12 + (i % 4) * 42, cyy = y + 28 + (i / 4) * 40;
                if (mouseX >= cx && mouseX <= cx + 34 && mouseY >= cyy && mouseY <= cyy + 34) {
                    ColorSetting target = findColor(colorTarget);
                    if (target != null) target.set(PALETTE[i]);
                    colorTarget = null;
                    return;
                }
            }
            int dx = x + 12 + 3 * 42 + 4;
            if (mouseX >= dx && mouseX <= dx + 34 && mouseY >= y + 28 + 3 * 40
                    && mouseY <= y + 28 + 3 * 40 + 34) {
                ColorSetting target = findColor(colorTarget);
                if (target != null) target.reset();
                colorTarget = null;
                return;
            }
            if (mouseX < x || mouseX > x + pw2 || mouseY < y || mouseY > y + ph2) colorTarget = null;
            return;
        }

        int sx = px + 7;
        int pad = 7;

        // search field
        int swH = 15;
        int sy = py + 26;
        if (mouseButton == 0 && mouseX >= sx && mouseX <= sx + sideW - 2 * pad
                && mouseY >= sy && mouseY <= sy + swH) {
            searchFocus = true;
            return;
        }
        searchFocus = false;

        // category nav
        Category[] cats = Category.values();
        int ny = py + 48;
        for (int i = 0; i < cats.length; i++) {
            if (mouseButton == 0 && mouseX >= sx && mouseX <= sx + sideW - 2 * pad
                    && mouseY >= ny && mouseY <= ny + 16) {
                selected = i;
                search = "";
                scrollOff = 0;
                expandedId = null;
                return;
            }
            ny += 18;
        }
        // HUD layout editor
        int hy = py + ph - 24;
        if (mouseButton == 0 && mouseX >= sx && mouseX <= sx + sideW - 2 * pad
                && mouseY >= hy && mouseY <= hy + 16) {
            mc.displayGuiScreen(new HudEditorScreen());
            return;
        }

        // scrollbar
        int content = contentH();
        int view = listBottom - listTop;
        if (content > view && mouseButton == 0) {
            int barX = px + pw - 7;
            int thumbH = Math.max(18, view * view / content);
            int thumbY = listTop + (view - thumbH) * scrollOff / maxScroll();
            if (mouseX >= barX - 2 && mouseX <= barX + 5
                    && mouseY >= thumbY - 2 && mouseY <= thumbY + thumbH + 2) {
                dragBar = true;
                grabBY = mouseY - thumbY;
                return;
            }
        }

        // module rows: head click = expand (or toggle when no settings);
        // switch = toggle; colour dot = palette
        for (Row r : rows) {
            if (mouseX < r.x || mouseX > r.x + r.w || mouseY < r.y || mouseY > r.y + r.h) continue;
            if (mouseY < listTop - 2 || mouseY > listBottom + 2) continue;
            Module m = r.module;
            if (mouseY <= r.y + 22) {
                int swW = 18;
                int swX = r.x + r.w - swW - 16;
                if (mouseX >= swX - 2 && mouseX <= swX + swW + 2) {
                    m.toggle();
                    return;
                }
                if (!visibleSettings(m).isEmpty()) {
                    expandedId = m.id.equals(expandedId) ? null : m.id;
                    clampScroll();
                } else {
                    m.toggle();
                }
                return;
            }
            for (Ctrl c : ctrls) {
                if (mouseX < c.x || mouseX > c.x + c.w || mouseY < c.y || mouseY > c.y + c.h) continue;
                Setting<?> s = c.set;
                if (s instanceof BooleanSetting) {
                    BooleanSetting b = (BooleanSetting) s;
                    b.set(!b.get());
                } else if (s instanceof ModeSetting) {
                    ModeSetting mode = (ModeSetting) s;
                    // click left half = previous mode, right half = next mode
                    int half = c.x + c.w / 2;
                    mode.set(mouseX < half
                            ? (mode.get() + mode.count() - 1) % mode.count()
                            : mode.next());
                } else if (s instanceof ColorSetting) {
                    colorTarget = m;
                } else if (s instanceof NumberSetting) {
                    dragSlider = c;
                    applySlider(c, mouseX);
                }
                return;
            }
            return;
        }
    }

    private void applySlider(Ctrl c, int mouseX) {
        NumberSetting s = (NumberSetting) c.set;
        int tW = c.w - 26;
        float t = (mouseX - c.x) / (float) tW;
        t = Math.max(0F, Math.min(1F, t));
        s.set(s.quantize(s.min + t * (s.max - s.min)));
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragBar) {
            int content = contentH();
            int view = listBottom - listTop;
            int thumbH = Math.max(18, view * view / content);
            int rel = mouseY - grabBY - listTop;
            scrollOff = maxScroll() > 0 ? rel * maxScroll() / (view - thumbH) : 0;
            clampScroll();
        } else if (dragSlider != null) {
            applySlider(dragSlider, mouseX);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragBar = false;
        dragSlider = null;
        ConfigManager.saveNow();
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        super.keyTyped(c, key);
        if (key == 1) {   // ESC
            if (colorTarget != null) colorTarget = null;
            else if (searchFocus) searchFocus = false;
            else {
                ConfigManager.saveNow();
                mc.displayGuiScreen(null);
            }
            return;
        }
        if (searchFocus) {
            if (key == 14 && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
            } else if (key == 28 || key == 15) {
                searchFocus = false;
            } else if (c >= 32 && c < 127) {
                search += c;
            }
            scrollOff = 0;
        }
    }
}
