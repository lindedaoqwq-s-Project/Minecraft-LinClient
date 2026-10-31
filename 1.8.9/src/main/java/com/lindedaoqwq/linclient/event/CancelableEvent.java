package com.lindedaoqwq.linclient.event;

/** Base for events a listener may cancel. Cancelling is a plain flag: dispatch keeps
 *  running, later listeners can inspect {@link #isCanceled()}. */
public abstract class CancelableEvent implements Event {
    private boolean canceled;

    public boolean isCanceled() { return canceled; }

    public void setCanceled(boolean v) { canceled = v; }
}
