package com.lindedaoqwq.linclient;

import com.lindedaoqwq.linclient.bridge.ForgeBridge;
import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.config.KeyBindings;
import com.lindedaoqwq.linclient.module.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LinClient - a practical, open-source, NON-CHEATING Minecraft client mod (Forge 1.8.9).
 *
 * Architecture (rewritten):
 *   module/    self-contained Module objects (base + 28 implementations)
 *   setting/   typed, persistence-aware settings with visibility conditions
 *   event/     own MethodHandle event bus; bridge/ForgeBridge is the only Forge listener
 *   config/    JSON store with schema version, backups and legacy .cfg import
 *   hud/       HUD render pipeline + drag editor
 *   gui/       single-window ClickGUI on the shared dark-glass theme
 *   hooks/     coremod byte-code hooks (see core/LinClientTransformer)
 *
 * Scope: client side only, no server behaviour changes, no unfair advantages.
 */
@Mod(modid = LinClient.MOD_ID, name = LinClient.MOD_NAME, version = LinClient.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.12.2]")
public class LinClient {
    public static final String MOD_ID = "linclient";
    public static final String MOD_NAME = "LinClient";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigManager.load();                 // JSON config (imports legacy .cfg once)
        ModuleManager.registerAll();          // instantiate + subscribe all modules
        ModuleManager.applyConfig();          // apply stored switches & settings
        KeyBindings.register();
        MinecraftForge.EVENT_BUS.register(new ForgeBridge());
        LOGGER.info("LinClient initialized (client, 1.12.2, {} modules).",
                ModuleManager.all().size());
    }
}
