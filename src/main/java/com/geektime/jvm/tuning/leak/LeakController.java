package com.geektime.jvm.tuning.leak;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convenience endpoints so you can start/stop/dump without restarting the app.
 * <p>
 * Note: still requires {@code jvm.leak.enabled=true}.
 */
@RestController
@RequestMapping("/jvm/leak")
@Profile("leak")
public class LeakController {

    private final JvmLeakProperties props;
    private final LeakSimulator simulator;

    public LeakController(JvmLeakProperties props, LeakSimulator simulator) {
        this.props = props;
        this.simulator = simulator;
    }

    @GetMapping("/start")
    public Map<String, Object> start() {
        simulator.start();
        return status();
    }

    @GetMapping("/stop")
    public Map<String, Object> stop() {
        simulator.stop();
        return status();
    }

    @GetMapping("/dump")
    public Map<String, Object> dump(@RequestParam(defaultValue = "true") boolean live) throws Exception {
        Map<String, Object> m = status();
        m.put("dumpPath", simulator.dumpHeapNow(live));
        return m;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", props.isEnabled());
        m.put("autoStart", props.isAutoStart());
        m.put("running", simulator.isRunning());
        m.put("bytesPerObject", props.getBytesPerObject());
        m.put("objectsPerSecond", props.getObjectsPerSecond());
        m.put("maxObjects", props.getMaxObjects());
        m.put("leakedObjects", simulator.leakedObjects());
        m.put("retainedObjects", LeakStore.retainedObjects());
        m.put("retainedBytes", LeakStore.retainedBytes());
        return m;
    }
}
