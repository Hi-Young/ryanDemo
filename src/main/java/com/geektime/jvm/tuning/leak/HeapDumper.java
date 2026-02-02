package com.geektime.jvm.tuning.leak;

import com.sun.management.HotSpotDiagnosticMXBean;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * Programmatic heap dump helper (same format as -XX:+HeapDumpOnOutOfMemoryError).
 */
public final class HeapDumper {

    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    private static volatile HotSpotDiagnosticMXBean hotspotMxBean;

    private HeapDumper() {
    }

    public static void dumpHeap(String filePath, boolean live) throws Exception {
        File f = new File(filePath);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create dump dir: " + parent.getAbsolutePath());
        }
        getHotspotMxBean().dumpHeap(f.getAbsolutePath(), live);
    }

    private static HotSpotDiagnosticMXBean getHotspotMxBean() throws Exception {
        if (hotspotMxBean != null) {
            return hotspotMxBean;
        }
        synchronized (HeapDumper.class) {
            if (hotspotMxBean == null) {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                hotspotMxBean = ManagementFactory.newPlatformMXBeanProxy(
                        server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
            }
        }
        return hotspotMxBean;
    }
}

