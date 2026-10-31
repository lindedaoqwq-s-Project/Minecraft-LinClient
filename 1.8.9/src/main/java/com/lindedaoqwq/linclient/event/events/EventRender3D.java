package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;

/** Fires at the end of world rendering (RenderWorldLastEvent): world-space drawing. */
public class EventRender3D implements Event {
    public final float partialTicks;

    public EventRender3D(float partialTicks) { this.partialTicks = partialTicks; }
}
