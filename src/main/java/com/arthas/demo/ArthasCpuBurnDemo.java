package com.arthas.demo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * A controlled CPU burner for Arthas profiler/trace practice.
 * <p>
 * Goal: create noticeable CPU usage but keep it bounded (duration/load/threads).
 */
@Service
public class ArthasCpuBurnDemo {

    public BurnResult burn(int threads, int durationMs, int loadPercent, int complexity) {
        int t = clamp(threads, 1, Math.max(1, Runtime.getRuntime().availableProcessors()));
        int d = clamp(durationMs, 100, 30_000);
        int l = clamp(loadPercent, 1, 100);
        int c = clamp(complexity, 1, 1_000_000);

        long startNs = System.nanoTime();

        if (t == 1) {
            BurnResult r = burnSingle(d, l, c);
            r.threads = 1;
            r.durationMs = d;
            r.loadPercent = l;
            r.complexity = c;
            r.elapsedMs = elapsedMs(startNs);
            return r;
        }

        ExecutorService pool = Executors.newFixedThreadPool(t - 1, newNamedDaemonFactory("arthas-cpu-burn"));
        List<Future<BurnResult>> futures = new ArrayList<>(t - 1);
        for (int i = 0; i < t - 1; i++) {
            futures.add(pool.submit(new Callable<BurnResult>() {
                @Override
                public BurnResult call() {
                    return burnSingle(d, l, c);
                }
            }));
        }

        // Also burn on the request thread so flamegraphs show the servlet thread as well.
        BurnResult merged = burnSingle(d, l, c);
        merged.threads = t;
        merged.durationMs = d;
        merged.loadPercent = l;
        merged.complexity = c;

        for (Future<BurnResult> f : futures) {
            try {
                BurnResult r = f.get();
                merged.ops += r.ops;
                merged.blackhole += r.blackhole;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException ignored) {
                // Demo endpoint: ignore a single worker failure and return what we have.
            }
        }

        pool.shutdownNow();
        merged.elapsedMs = elapsedMs(startNs);
        return merged;
    }

    private static BurnResult burnSingle(int durationMs, int loadPercent, int complexity) {
        // 20ms control period gives a stable-ish CPU load curve without being too jittery.
        final long periodNs = TimeUnit.MILLISECONDS.toNanos(20);
        final long busyNs = periodNs * loadPercent / 100;
        final long endNs = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(durationMs);

        long ops = 0;
        long blackhole = 0;

        while (System.nanoTime() < endNs) {
            long cycleStart = System.nanoTime();

            // Busy section: do pure CPU work.
            while (System.nanoTime() - cycleStart < busyNs) {
                blackhole = CpuWork.mix(blackhole, ops, complexity);
                ops++;
            }

            // Sleep section: yield CPU to control utilization.
            long spent = System.nanoTime() - cycleStart;
            long rest = periodNs - spent;
            if (rest > 0) {
                LockSupport.parkNanos(rest);
            }
        }

        BurnResult r = new BurnResult();
        r.ops = ops;
        r.blackhole = blackhole;
        return r;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static long elapsedMs(long startNs) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
    }

    private static ThreadFactory newNamedDaemonFactory(String prefix) {
        AtomicInteger idx = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + "-" + idx.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    /**
     * Intentionally "mathy" work that stays on CPU and avoids allocations.
     */
    static final class CpuWork {
        private CpuWork() {
        }

        static long mix(long seed, long step, int rounds) {
            long x = seed ^ (step * 0x9E3779B97F4A7C15L);
            for (int i = 0; i < rounds; i++) {
                x ^= (x << 13);
                x ^= (x >>> 7);
                x ^= (x << 17);
                x += 0xBF58476D1CE4E5B9L;
            }
            return x;
        }
    }

    public static final class BurnResult {
        public int threads;
        public int durationMs;
        public int loadPercent;
        public int complexity;
        public long elapsedMs;
        public long ops;
        public long blackhole;
    }
}

