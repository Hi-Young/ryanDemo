package com.geektime.jvm.tuning.leak;

/**
 * A simple retained object to make the leak easy to spot in a heap dump.
 */
public class LeakItem {

    private final long id;
    private final long createdAtMillis;
    private final byte[] payload;
    private final String tag;

    public LeakItem(long id, byte[] payload, String tag) {
        this.id = id;
        this.payload = payload;
        this.tag = tag;
        this.createdAtMillis = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getTag() {
        return tag;
    }
}

