package cn.pupperclient.skimca.event;

public abstract class Event {
    private boolean cancelled = false;

    /**
     * Checks whether this event has been cancelled.
     *
     * @return {@code true} if the event is cancelled, {@code false} otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of this event.
     *
     * @param cancelled the new cancelled state
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Cancels this event.
     * Equivalent to calling {@code setCancelled(true)}.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Uncancels this event.
     * Equivalent to calling {@code setCancelled(false)}.
     */
    public void uncancel() {
        this.cancelled = false;
    }
}
