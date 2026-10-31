package com.lindedaoqwq.linclient.event.events;

import com.lindedaoqwq.linclient.event.Event;
import net.minecraft.entity.Entity;

/** The local player attacked an entity (pre-damage). */
public class EventAttack implements Event {
    public final Entity target;

    public EventAttack(Entity target) { this.target = target; }
}
