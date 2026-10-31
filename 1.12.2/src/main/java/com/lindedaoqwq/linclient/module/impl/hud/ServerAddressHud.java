package com.lindedaoqwq.linclient.module.impl.hud;

import com.lindedaoqwq.linclient.module.Category;
import com.lindedaoqwq.linclient.module.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/** Address of the server currently joined (hidden in singleplayer). */
public class ServerAddressHud extends HudModule {
    public ServerAddressHud() {
        super("serveraddress", Category.HUD, "Server Address", "\u670d\u52a1\u5668\u5730\u5740",
                "Address of the server you are on", "\u5f53\u524d\u8fde\u63a5\u7684\u670d\u52a1\u5668\u5730\u5740",
                Anchor.RIGHT_TOP, Trait.BACKGROUND);
    }

    private String line(Minecraft mc, boolean edit) {
        String ip = null;
        ServerData sd = mc.getCurrentServerData();
        if (sd != null && sd.serverIP != null && !sd.serverIP.isEmpty()) ip = sd.serverIP;
        return orPreview(edit, ip == null ? null : "\u00A7bServer: \u00A7f" + ip);
    }

    @Override
    public void render(Minecraft mc, int x, int y, boolean editMode) {
        String s = line(mc, editMode);
        if (s != null) drawText(mc, s, x, y);
    }

    @Override
    public int width(Minecraft mc) { return textWidth(mc, line(mc, false)); }

    @Override
    public int height(Minecraft mc) { return 11; }
}
