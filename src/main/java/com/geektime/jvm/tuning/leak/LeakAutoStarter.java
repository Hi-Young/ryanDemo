package com.geektime.jvm.tuning.leak;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Starts the leak on startup (opt-in via properties).
 */
@Component
@Profile("leak")
public class LeakAutoStarter {

    private final JvmLeakProperties props;
    private final LeakSimulator simulator;

    public LeakAutoStarter(JvmLeakProperties props, LeakSimulator simulator) {
        this.props = props;
        this.simulator = simulator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (props.isEnabled() && props.isAutoStart()) {
            simulator.start();
        }
    }
}
