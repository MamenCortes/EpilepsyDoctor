package Events;

import com.google.common.eventbus.EventBus;
/**
 * Provides a globally accessible event bus for UI-level communication.
 * <p>
 * This class exposes a singleton {@link EventBus} instance used for posting
 * and subscribing to application-wide UI events, such as disconnection
 * notifications or updates involving server interactions.
 * </p>
 * <p>
 * This class cannot be instantiated; use {@link #BUS} directly.
 * </p>
 */
public class UIEventBus {

    /**
     * Singleton global event bus used across the application for UI events.
     */
    public static final EventBus BUS = new EventBus();

    private UIEventBus() {} // no instantiation
}