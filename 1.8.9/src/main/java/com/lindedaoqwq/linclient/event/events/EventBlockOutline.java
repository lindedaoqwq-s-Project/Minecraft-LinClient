package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.CancelableEvent;
import net.minecraft.util.MovingObjectPosition;

/** Block selection outline draw (DrawBlockHighlightEvent). Cancel to replace the
 *  vanilla outline with a custom one. */
public class EventBlockOutline extends CancelableEvent {
    public final MovingObjectPosition target;
    public final float partialTicks;

    public EventBlockOutline(MovingObjectPosition target, float partialTicks) {
        this.target = target;
        this.partialTicks = partialTicks;
    }
}
