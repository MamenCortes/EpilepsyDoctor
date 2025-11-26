package Events;

import com.google.common.eventbus.EventBus;

public class UIEventBus {

    // Global singleton event bus
    public static final EventBus BUS = new EventBus();

    private UIEventBus() {} // no instantiation
}