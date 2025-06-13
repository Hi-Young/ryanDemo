package com.geektime.concurrent.basic;

public class DoubleCheckLocker {
    private static volatile DoubleCheckLocker doubleCheckLocker;

    private static Object object = new Object();

    private DoubleCheckLocker() {

    }

    public static DoubleCheckLocker getDoubleCheckLocker() {

        if (doubleCheckLocker == null) {
            synchronized (object) {
                if (doubleCheckLocker == null) {
                    doubleCheckLocker = new DoubleCheckLocker();
                }
            }
        }

        return doubleCheckLocker;
    }
}

