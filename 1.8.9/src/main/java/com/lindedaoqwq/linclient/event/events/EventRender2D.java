package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;

/** Fires once per rendered frame after the game overlay (ElementType.ALL pass). */
public class EventRender2D implements Event {
    public final float partialTicks;

    public EventRender2D(float partialTicks) { this.partialTicks = partialTicks; }
}
