package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.CancelableEvent;

/** Player FOV modifier event.
 *  {@code baseFov} is the FOV before any speed/movement modification.
 *  {@code newFov} is the FOV that will be applied; listeners may rewrite it.
 *  Cancelling means "keep {@code baseFov}" (used to remove the speed FOV effect). */
public class EventFov extends CancelableEvent {
    public final float baseFov;
    public float newFov;

    public EventFov(float baseFov, float newFov) {
        this.baseFov = baseFov;
        this.newFov = newFov;
    }
}
