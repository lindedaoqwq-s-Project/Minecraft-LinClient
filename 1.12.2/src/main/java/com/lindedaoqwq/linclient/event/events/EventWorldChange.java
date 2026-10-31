package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;

/** The world reference changed: joined a world/server or disconnected. */
public class EventWorldChange implements Event {
    /** true = now inside a world, false = left/disconnected. */
    public final boolean joined;

    public EventWorldChange(boolean joined) { this.joined = joined; }
}
