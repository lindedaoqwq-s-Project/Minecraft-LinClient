package com.lindedaoqwq.linclient.state;

import java.util.ArrayDeque;

/**
 * Runtime, non-persistent client state shared by modules and the bridge: frame
 * counters, click queues, session stats. Nothing here is sent to or read from the
 * server. Per-module state (combo, zoom, ...) lives inside its module class.
 */
public class ClientState {
    public static boolean modActive = true;
    public static boolean hudEnabled = true;

    // Movement (updated by ForgeBridge each tick)
    public static double speed = 0.0;        // blocks / second
    public static double distance = 0.0;     // total horizontal distance travelled
    public static double prevX = 0, prevY = 0, prevZ = 0;
    public static boolean hasPrev = false;

    // Session
    public static long sessionStart = System.currentTimeMillis();   // playtime module

    // Input / clicks (updated by ForgeBridge on mouse events)
    public static boolean leftHeld = false, rightHeld = false;
    public static final ArrayDeque<Long> leftClickTimes = new ArrayDeque<Long>();
    public static final ArrayDeque<Long> rightClickTimes = new ArrayDeque<Long>();

    // Self latency, ms (updated by ForgeBridge each tick)
    public static int ping = 0;

    // FPS is self-counted every rendered frame and computed once per second.
    public static int fps = 0;
    private static int frameCounter = 0;
    private static long fpsTimestamp = System.currentTimeMillis();

    /** Call once per rendered frame. Counts frames and reports the smoothed
     *  frames-per-second every ~1s. */
    public static void refreshFps() {
        frameCounter++;
        long now = System.currentTimeMillis();
        long elapsed = now - fpsTimestamp;
        if (elapsed >= 1000L) {
            fps = (int) (frameCounter * 1000L / elapsed);
            frameCounter = 0;
            fpsTimestamp = now;
        }
    }

    public static void addLeftClick() {
        long now = System.currentTimeMillis();
        leftClickTimes.addLast(now);
        prune(leftClickTimes, now);
    }

    public static void addRightClick() {
        long now = System.currentTimeMillis();
        rightClickTimes.addLast(now);
        prune(rightClickTimes, now);
    }

    public static int leftCps() {
        prune(leftClickTimes, System.currentTimeMillis());
        return leftClickTimes.size();
    }

    public static int rightCps() {
        prune(rightClickTimes, System.currentTimeMillis());
        return rightClickTimes.size();
    }

    private static void prune(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000L) q.pollFirst();
    }
}
