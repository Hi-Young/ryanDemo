package com.ryan.jvm.Test;

/**
 * @author heyyon
 * @date 2025-02-04 10:48
 */
public class TestDeadLock {
    private static Object obj1 = new Object();
    private static Object obj2 = new Object();

    public static void main(String[] args) {
        new Thread(new Thread1()).start();//启动线程01
        new Thread(new Thread2()).start();//启动线程02
    }

    //线程01
    private static class Thread1 implements Runnable {
        @Override
        public void run() {
            synchronized (obj1) {
                System.out.println("Thread1 拿到了 obj1 的锁！");
                try {
// 停顿2秒的意义在于，让Thread2线程拿到obj2的锁
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                synchronized (obj2) {
                    System.out.println("Thread1 拿到了 obj2 的锁！");
                }
            }
        }
    }

    //线程02
    private static class Thread2 implements Runnable {
        @Override
        public void run() {
            synchronized (obj2) {
                System.out.println("Thread2 拿到了 obj2 的锁！");
                try {
// 停顿2秒的意义在于，让Thread1线程拿到obj1的锁
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj1) {
                    System.out.println("Thread2 拿到了 obj1 的锁！");
                }
            }
        }
    }
}
