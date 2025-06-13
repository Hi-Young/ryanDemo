package com.geektime.concurrent.lock;

public class CustomLockTest {
    private static int counter = 0;
    private static SimpleCustomLock lock = new SimpleCustomLock();

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Worker(), "Thread-" + (i + 1));
            threads[i].start();
        }

        // 添加监控线程
        Thread monitorThread = new Thread(() -> {
            while (true) {
                System.out.println("\nQueue status: " + lock.getQueueInfo());
                try {
                    Thread.sleep(100);  // 每100ms打印一次队列状态
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "Monitor-Thread");
        monitorThread.setDaemon(true);
        monitorThread.start();

        // 等待工作线程完成
        for (Thread t : threads) {
            t.join();
        }
        monitorThread.interrupt();
        System.out.println("Final counter value: " + counter);
    }

    static class Worker implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                try {
                    lock.lock();  // 在这里设置断点
                    // 模拟工作负载
                    Thread.sleep(1);  // 增加一点延迟使竞争更明显
                    counter++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
