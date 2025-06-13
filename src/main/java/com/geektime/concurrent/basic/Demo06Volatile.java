package com.geektime.concurrent.basic;

/**
 * @author heyyon
 * @date 2025-02-05 22:34
 */
public class Demo06Volatile {
    public static void main(String[] args) throws InterruptedException {
        VolatileDemo demo = new VolatileDemo();
        for (int i = 0; i < 2; i++) {

            Thread t = new Thread(demo);
            t.start();
        }
        Thread.sleep(1000);
        System.out.println("count = " + demo.count);
    }

    static class VolatileDemo implements Runnable {
        public volatile int count;

        //public volatile AtomicInteger count = new AtomicInteger(0);
        public void run() {
            addCount();
        }

        public void addCount() {
            for (int i = 0; i < 10000; i++) {
                count++;//但是实际情况是三条汇编指令
            }
        }
    }
}

