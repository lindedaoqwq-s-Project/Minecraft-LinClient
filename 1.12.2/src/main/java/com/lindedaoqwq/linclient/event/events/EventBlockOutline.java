package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.CancelableEvent;
import net.minecraft.util.math.RayTraceResult;

/** Block selection outline draw (DrawBlockHighlightEvent). Cancel to replace the
 *  vanilla outline with a custom one. */
public class EventBlockOutline extends CancelableEvent {
    public final RayTraceResult target;
    public final float partialTicks;

    public EventBlockOutline(RayTraceResult target, float partialTicks) {
        this.target = target;
        this.partialTicks = partialTicks;
    }
}
