package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;

/** The local player took damage. */
public class EventHurt implements Event {
    public final float amount;

    public EventHurt(float amount) { this.amount = amount; }
}
