package com.geektime.jvm.tuning.leak;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * The actual leak: a static map that grows forever and keeps objects alive via a GC root.
 */
public final class LeakStore {

    private static final ConcurrentHashMap<Long, LeakItem> STORE = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GEN = new AtomicLong(0);
    private static final LongAdder RETAINED_BYTES = new LongAdder();

    private LeakStore() {
    }

    public static LeakItem leakOne(int bytesPerObject, String tag) {
        long id = ID_GEN.incrementAndGet();
        byte[] payload = new byte[Math.max(1, bytesPerObject)];
        LeakItem item = new LeakItem(id, payload, tag);
        STORE.put(id, item);
        RETAINED_BYTES.add(payload.length);
        return item;
    }

    public static int retainedObjects() {
        return STORE.size();
    }

    public static long retainedBytes() {
        return RETAINED_BYTES.sum();
    }
}

