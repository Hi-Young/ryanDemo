package com.geektime.jvm.tuning.leak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("leak")
public class LeakSimulator {

    private static final Logger log = LoggerFactory.getLogger(LeakSimulator.class);

    private final JvmLeakProperties props;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong leakedObjects = new AtomicLong(0);
    private volatile ExecutorService executor;

    public LeakSimulator(JvmLeakProperties props) {
        this.props = props;
    }

    public boolean isRunning() {
        return running.get();
    }

    public long leakedObjects() {
        return leakedObjects.get();
    }

    public void start() {
        if (!props.isEnabled()) {
            log.info("jvm.leak.enabled=false; leak simulator not started.");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            return;
        }

        executor = Executors.newSingleThreadExecutor(newDaemonFactory("jvm-leak-simulator"));
        executor.submit(this::leakLoop);
        log.warn("Leak simulator started: bytesPerObject={}, objectsPerSecond={}, maxObjects={}",
                props.getBytesPerObject(), props.getObjectsPerSecond(), props.getMaxObjects());
    }

    public void stop() {
        running.set(false);
        ExecutorService ex = executor;
        if (ex != null) {
            ex.shutdownNow();
        }
        log.warn("Leak simulator stopped. retainedObjects={}, retainedBytes={}, leakedObjects={}",
                LeakStore.retainedObjects(), LeakStore.retainedBytes(), leakedObjects.get());
    }

    public String dumpHeapNow(boolean live) throws Exception {
        String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String path = props.getDumpDir() + FileSeparator.FILE_SEPARATOR + "heap-" + ts + ".hprof";
        HeapDumper.dumpHeap(path, live);
        return path;
    }

    private void leakLoop() {
        int ops = Math.max(1, props.getObjectsPerSecond());
        long max = props.getMaxObjects();

        // Sleep between allocations; we prefer a stable leak rate for GC observation.
        long sleepMs = Math.max(1L, 1000L / ops);

        while (running.get()) {
            long current = leakedObjects.incrementAndGet();
            LeakStore.leakOne(props.getBytesPerObject(), "jvm-tuning-leak");

            if (current % 50 == 0) {
                log.warn("Leaking... leakedObjects={}, retainedObjects={}, retainedBytes={}",
                        current, LeakStore.retainedObjects(), LeakStore.retainedBytes());
            }

            if (max >= 0 && current >= max) {
                log.warn("Reached jvm.leak.max-objects={}; stopping allocations (retained objects stay).", max);
                running.set(false);
                break;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @PreDestroy
    public void onShutdown() {
        stop();
    }

    private static ThreadFactory newDaemonFactory(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }

    /**
     * Avoid platform-specific separators in business logic.
     */
    private static final class FileSeparator {
        private static final String FILE_SEPARATOR = System.getProperty("file.separator", "/");
    }
}
