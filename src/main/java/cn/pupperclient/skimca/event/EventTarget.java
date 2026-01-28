package cn.pupperclient.skimca.event;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {
    /**
     * The priority of this event handler.
     * Handlers with higher priority are executed before those with lower priority.
     *
     * @return the priority level
     */
    Priority priority() default Priority.NORMAL;

    /**
     * Whether this handler should receive cancelled events.
     *
     * @return {@code true} if the handler should receive cancelled events,
     *         {@code false} otherwise
     */
    boolean ignoreCancelled() default false;

    enum Priority {
        /** Lowest priority, executed last. */
        LOWEST,
        /** Low priority. */
        LOW,
        /** Normal/default priority. */
        NORMAL,
        /** High priority. */
        HIGH,
        /** Highest priority, executed first. */
        HIGHEST,
        /** Used for monitoring purposes only, should not modify events. */
        MONITOR
    }
}
