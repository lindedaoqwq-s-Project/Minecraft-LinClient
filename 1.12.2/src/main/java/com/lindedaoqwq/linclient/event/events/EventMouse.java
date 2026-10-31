package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;

/** Raw mouse button event: button 0 = left, 1 = right; state = pressed. */
public class EventMouse implements Event {
    public final int button;
    public final boolean state;

    public EventMouse(int button, boolean state) {
        this.button = button;
        this.state = state;
    }
}
