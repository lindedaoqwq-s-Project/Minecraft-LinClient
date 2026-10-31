package com.lindedaoqwq.linclient.bridge;

import com.lindedaoqwq.linclient.config.ConfigManager;
import com.lindedaoqwq.linclient.config.KeyBindings;
import com.lindedaoqwq.linclient.event.EventBus;
import com.lindedaoqwq.linclient.event.events.EventAttack;
import com.lindedaoqwq.linclient.event.events.EventBlockOutline;
import com.lindedaoqwq.linclient.event.events.EventCamera;
import com.lindedaoqwq.linclient.event.events.EventFov;
import com.lindedaoqwq.linclient.event.events.EventHurt;
import com.lindedaoqwq.linclient.event.events.EventMouse;
import com.lindedaoqwq.linclient.event.events.EventRender2D;
import com.lindedaoqwq.linclient.event.events.EventRender3D;
import com.lindedaoqwq.linclient.event.events.EventTick;
import com.lindedaoqwq.linclient.event.events.EventWorldChange;
import com.lindedaoqwq.linclient.gui.ClickGuiScreen;
import com.lindedaoqwq.linclient.hooks.EngineHooks;
import com.lindedaoqwq.linclient.hud.HudEditorScreen;
import com.lindedaoqwq.linclient.module.ModuleManager;
import com.lindedaoqwq.linclient.module.impl.render.Crosshair;
import com.lindedaoqwq.linclient.state.ClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * The only class with Forge {@code @SubscribeEvent} methods. Translates Forge events
 * into {@link EventBus} events (and handles the few pure-UI/engine concerns directly:
 * main-menu replacement, the pause-menu button, vanilla crosshair suppression).
 *
 * This is also the single place where Forge/MC API names differ between versions,
 * which keeps the 1.12.2 sed-translation surface small.
 */
public class ForgeBridge {
    private WorldClient lastWorld = null;
    private boolean langSynced = false;

    private static final int BTN_LINCLIENT = 997;

    // ---------- render ----------

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            ClientState.refreshFps();
            EngineHooks.onFrame();
            EventBus.dispatch(new EventRender2D(event.getPartialTicks()));
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(net.minecraftforge.client.event.RenderWorldLastEvent event) {
        EventBus.dispatch(new EventRender3D(event.getPartialTicks()));
    }

    @SubscribeEvent
    public void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        // Direct query instead of an event: the vanilla crosshair is suppressed when
        // the Crosshair module is on (intentional shortcut, not worth an event type).
        Crosshair crosshair = ModuleManager.get(Crosshair.class);
        if (crosshair != null && crosshair.isEnabled()
                && event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
        }
    }

    // ---------- tick ----------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();

        // World switch detection -> session reset event.
        if (mc.world != lastWorld) {
            boolean joined = mc.world != null;
            lastWorld = mc.world;
            ClientState.ping = 0;
            ClientState.distance = 0;
            ClientState.hasPrev = false;
            EngineHooks.clearCaches();
            EventBus.dispatch(new EventWorldChange(joined));
        }

        // Sync config language from the game language once.
        if (!langSynced) {
            langSynced = true;
            try {
                ConfigManager.setLang(mc.gameSettings.language);
            } catch (Throwable ignored) { }
        }

        // Global key bindings.
        if (KeyBindings.OPEN_CONFIG.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(new ClickGuiScreen());
        }
        if (KeyBindings.OPEN_HOME.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(new HudEditorScreen());
        }
        if (KeyBindings.TOGGLE_MOD.isPressed()) {
            ClientState.modActive = !ClientState.modActive;
        }
        if (KeyBindings.TOGGLE_HUD.isPressed()) {
            ClientState.hudEnabled = !ClientState.hudEnabled;
        }

        tickMovement(mc);
        updatePing(mc);
        ConfigManager.tickSave();
        EventBus.dispatch(new EventTick(mc));
    }

    private void tickMovement(Minecraft mc) {
        EntityPlayerSP p = mc.player;
        if (p == null) return;
        if (!ClientState.hasPrev) {
            ClientState.prevX = p.posX;
            ClientState.prevY = p.posY;
            ClientState.prevZ = p.posZ;
            ClientState.hasPrev = true;
            return;
        }
        double dx = p.posX - ClientState.prevX;
        double dz = p.posZ - ClientState.prevZ;
        double dh = Math.sqrt(dx * dx + dz * dz);
        ClientState.distance += dh;
        ClientState.speed = dh * 20.0;   // 20 ticks per second
        ClientState.prevX = p.posX;
        ClientState.prevY = p.posY;
        ClientState.prevZ = p.posZ;
    }

    private void updatePing(Minecraft mc) {
        ClientState.ping = 0;
        if (mc.getConnection() != null && mc.player != null) {
            net.minecraft.client.network.NetworkPlayerInfo info =
                    mc.getConnection().getPlayerInfo(mc.player.getUniqueID());
            if (info != null && info.getResponseTime() > 0) ClientState.ping = info.getResponseTime();
        }
    }

    // ---------- entities ----------

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || event.getEntity() != mc.player) return;
        EventBus.dispatch(new EventAttack(event.getTarget()));
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && event.getEntity() == mc.player) {
            EventBus.dispatch(new EventHurt(event.getAmount()));
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        int btn = event.getButton();
        boolean state = event.isButtonstate();
        if (btn == 0 && state) {
            ClientState.addLeftClick();
            ClientState.leftHeld = true;
        } else if (btn == 0) {
            ClientState.leftHeld = false;
        }
        if (btn == 1 && state) {
            ClientState.addRightClick();
            ClientState.rightHeld = true;
        } else if (btn == 1) {
            ClientState.rightHeld = false;
        }
        EventBus.dispatch(new EventMouse(btn, state));
    }

    // ---------- view ----------

    @SubscribeEvent
    public void onFov(FOVUpdateEvent event) {
        EventFov ev = new EventFov(event.getFov(), event.getNewfov());
        EventBus.dispatch(ev);
        if (ev.isCanceled()) {
            event.setNewfov(ev.baseFov);          // cancel = keep the unmodified FOV
        } else {
            event.setNewfov(ev.newFov);           // listeners may have rewritten it
        }
    }

    @SubscribeEvent
    public void onCamera(EntityViewRenderEvent.CameraSetup event) {
        EventCamera ev = new EventCamera(event.getYaw(), event.getPitch(), event.getRoll());
        EventBus.dispatch(ev);
        event.setYaw(ev.yaw);
        event.setPitch(ev.pitch);
        event.setRoll(ev.roll);
    }

    @SubscribeEvent
    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        EventBlockOutline ev = new EventBlockOutline(event.getTarget(), event.getPartialTicks());
        EventBus.dispatch(ev);
        if (ev.isCanceled()) event.setCanceled(true);
    }

    // ---------- screens (handled here directly) ----------

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu) {
            event.setGui(new com.lindedaoqwq.linclient.gui.LinMainMenu());
        }
    }

    @SubscribeEvent
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiIngameMenu || gui instanceof GuiOptions) {
            int bw = 120, bh = 20;
            event.getButtonList().add(new GuiButton(BTN_LINCLIENT, gui.width - bw - 6, 6, bw, bh, "LinClient"));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getButton() != null && event.getButton().id == BTN_LINCLIENT) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGuiScreen());
        }
    }
}
