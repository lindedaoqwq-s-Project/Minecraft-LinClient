package com.lindedaoqwq.linclient.event;

import com.lindedaoqwq.linclient.LinClient;
import com.lindedaoqwq.linclient.module.Module;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal client event bus. Routes {@link Event} instances to methods marked with
 * {@link Subscribe} using MethodHandles (near-direct-call speed, no per-dispatch
 * reflection). Design notes, deliberately different from the reference client:
 *
 *  - Listeners register ONCE at startup and stay registered. Handlers of modules that
 *    are currently disabled are skipped in {@link #dispatch}. Enable-time registration
 *    is the classic source of duplicate handlers and forgotten unregisters.
 *  - Each handler invocation is isolated: a throwing listener is logged and skipped,
 *    never breaking the others or the caller.
 *  - Routing is exact-class (all our events are leaf types), no inheritance walk.
 */
public final class EventBus {
    private static final Map<Class<?>, List<Handler>> ROUTES =
            new ConcurrentHashMap<Class<?>, List<Handler>>();

    private EventBus() { }

    /** Scan {@code listener}'s public methods for {@link Subscribe} and register them.
     *  Registering the same object twice is a no-op (identity-based). */
    public static void register(Object listener) {
        for (List<Handler> list : ROUTES.values()) {
            for (Handler h : list) {
                if (h.owner == listener) return;
            }
        }
        for (Method m : listener.getClass().getMethods()) {
            if (!m.isAnnotationPresent(Subscribe.class)) continue;
            if (m.getParameterTypes().length != 1) {
                LinClient.LOGGER.warn("Skipping @Subscribe {}::{}: must take exactly one event", listener, m.getName());
                continue;
            }
            Class<?> type = m.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(type)) {
                LinClient.LOGGER.warn("Skipping @Subscribe {}::{}: parameter is not an Event", listener, m.getName());
                continue;
            }
            try {
                m.setAccessible(true);
                MethodHandle handle = MethodHandles.lookup().unreflect(m)
                        .bindTo(listener)
                        .asType(MethodType.methodType(void.class, Event.class));
                List<Handler> list = ROUTES.get(type);
                if (list == null) {
                    list = new CopyOnWriteArrayList<Handler>();
                    ROUTES.putIfAbsent(type, list);
                    list = ROUTES.get(type);
                }
                list.add(new Handler(listener, m.getName(), handle));
            } catch (Throwable t) {
                LinClient.LOGGER.warn("Failed to bind listener {}::{}", listener, m.getName(), t);
            }
        }
    }

    /** Remove every handler owned by {@code listener} (identity based). */
    public static void unregister(Object listener) {
        for (Map.Entry<Class<?>, List<Handler>> e : ROUTES.entrySet()) {
            List<Handler> keep = new ArrayList<Handler>();
            for (Handler h : e.getValue()) {
                if (h.owner != listener) keep.add(h);
            }
            e.getValue().clear();
            e.getValue().addAll(keep);
        }
    }

    /** Dispatch to handlers of the event's exact class. Skips disabled modules and
     *  isolates listener failures. */
    public static void dispatch(Event event) {
        List<Handler> list = ROUTES.get(event.getClass());
        if (list == null || list.isEmpty()) return;
        for (Handler h : list) {
            if (h.owner instanceof Module && !((Module) h.owner).isEnabled()) continue;
            try {
                h.handle.invoke(event);
            } catch (Throwable t) {
                LinClient.LOGGER.warn("Event handler {}::{} failed for {}",
                        h.owner.getClass().getSimpleName(), h.name, event.getClass().getSimpleName(), t);
            }
        }
    }

    private static final class Handler {
        final Object owner;
        final String name;
        final MethodHandle handle;

        Handler(Object owner, String name, MethodHandle handle) {
            this.owner = owner;
            this.name = name;
            this.handle = handle;
        }
    }
}
