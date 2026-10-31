package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.CancelableEvent;

/** Camera orientation update (EntityViewRenderEvent.CameraSetup). Listeners may rewrite
 *  yaw/pitch (FreeLook) or roll. */
public class EventCamera extends CancelableEvent {
    public float yaw;
    public float pitch;
    public float roll;

    public EventCamera(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }
}
