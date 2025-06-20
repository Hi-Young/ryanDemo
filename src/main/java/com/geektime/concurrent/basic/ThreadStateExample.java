package com.geektime.concurrent.basic;

/**
 * 这个问题问了多个AI,测试结果如下：
 * CHATGPT: O3-MINI 错, O3-MINI-HIGH 对,  O1对,4O错
 * 豆包错
 * kimi错
 * 通义2.5MAX对
 * Claude错
 * deepseek对
 * 谷歌 2.0pro exp错，2.0flash对
 */
public class ThreadStateExample {

    private static final Object lock = new Object(); // 用于线程之间的同步

    public static void main(String[] args) throws InterruptedException {

        // 创建线程1
        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("Thread 1: Holding lock...");
                    Thread.sleep(5000);  // 模拟线程1在执行一些任务
                    System.out.println("Thread 1: Released lock.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread.sleep(10);

        // 创建线程2
        Thread thread2 = new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("Thread 2: Attempting to acquire lock.");
                    Thread.sleep(1000);  // 稍微延迟一下，确保线程1先获取到锁
                    System.out.println("Thread 2: Got the lock.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程执行完毕
        thread1.join();
        thread2.join();
    }
}

