package com.geektime.jvm.tuning.leak;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Intentionally creates a heap memory leak for JVM tuning / MAT practice.
 * <p>
 * Opt-in only: set {@code jvm.leak.enabled=true}.
 */
@Component
@ConfigurationProperties(prefix = "jvm.leak")
public class JvmLeakProperties {

    /**
     * Master switch. When false, nothing will start/allocate.
     */
    private boolean enabled = false;

    /**
     * When true, start leaking automatically after Spring is ready.
     */
    private boolean autoStart = false;

    /**
     * Size of each leaked payload (bytes).
     */
    private int bytesPerObject = 1024 * 1024; // 1MB

    /**
     * Leak rate (objects per second).
     */
    private int objectsPerSecond = 5;

    /**
     * Stop after leaking N objects; -1 means keep leaking until OOM.
     */
    private long maxObjects = -1;

    /**
     * Directory for on-demand heap dumps.
     */
    private String dumpDir = "dumps";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public int getBytesPerObject() {
        return bytesPerObject;
    }

    public void setBytesPerObject(int bytesPerObject) {
        this.bytesPerObject = bytesPerObject;
    }

    public int getObjectsPerSecond() {
        return objectsPerSecond;
    }

    public void setObjectsPerSecond(int objectsPerSecond) {
        this.objectsPerSecond = objectsPerSecond;
    }

    public long getMaxObjects() {
        return maxObjects;
    }

    public void setMaxObjects(long maxObjects) {
        this.maxObjects = maxObjects;
    }

    public String getDumpDir() {
        return dumpDir;
    }

    public void setDumpDir(String dumpDir) {
        this.dumpDir = dumpDir;
    }
}

