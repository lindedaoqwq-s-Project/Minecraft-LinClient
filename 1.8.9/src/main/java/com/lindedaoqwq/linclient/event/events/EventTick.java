package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;
import net.minecraft.client.Minecraft;

/** Fires once per client tick (END phase). The player/world may be null. */
public class EventTick implements Event {
    public final Minecraft mc;

    public EventTick(Minecraft mc) { this.mc = mc; }
}
