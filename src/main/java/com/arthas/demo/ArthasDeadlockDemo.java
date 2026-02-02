package com.arthas.demo;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates a classic two-thread deadlock for learning jstack / Arthas thread -b.
 * <p>
 * This is intentionally unrecoverable without restarting the JVM.
 */
@Service
public class ArthasDeadlockDemo {

    public static final String THREAD_1_NAME = "arthas-deadlock-1";
    public static final String THREAD_2_NAME = "arthas-deadlock-2";

    private final Object lockA = new Object();
    private final Object lockB = new Object();
    private final AtomicBoolean started = new AtomicBoolean(false);

    public boolean isStarted() {
        return started.get();
    }

    /**
     * @return true if deadlock threads are created this time; false if already started.
     */
    public boolean startDeadlock() {
        if (!started.compareAndSet(false, true)) {
            return false;
        }

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                sleepQuietly(200);
                synchronized (lockB) {
                    // unreachable
                }
            }
        }, THREAD_1_NAME);

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                sleepQuietly(200);
                synchronized (lockA) {
                    // unreachable
                }
            }
        }, THREAD_2_NAME);

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        return true;
    }

    private static void sleepQuietly(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

