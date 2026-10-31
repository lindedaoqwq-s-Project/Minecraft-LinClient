package com.lindedaoqwq.linclient.module;

/** Module categories with bilingual labels. */
public enum Category {
    HUD("HUD", "HUD"),
    COMBAT("Combat", "\u6218\u6597"),
    MOVEMENT("Movement", "\u79fb\u52a8"),
    PLAYER("Player", "\u73a9\u5bb6"),
    RENDER("Render", "\u6e32\u67d3");

    public final String label;
    public final String labelZh;

    Category(String label, String labelZh) {
        this.label = label;
        this.labelZh = labelZh;
    }

    public String text(boolean zh) { return zh ? labelZh : label; }
}
