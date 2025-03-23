package com.ryan.study.thread;

public class DeadLockTest {
    
    static Object lock1 = new Object();
    
    static Object lock2 = new Object();
    
    public void test() {
        
        
    }

    public static void main(String[] args) {
        new Thread(new Runnable() {
            public void run() {
                synchronized (lock1) {
                    System.out.println("thread1 acquired lock1");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (lock2) {
                        System.out.println("thread1 acquired lock2");
                    }
                }
                
            }
        }).start();


        new Thread(new Runnable() {
            public void run() {
                synchronized (lock2) {
                    System.out.println("thread2 acquired lock2");
//                    try {
////                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
                    synchronized (lock1) {
                        System.out.println("thread2 acquired lock1");
                    }
                }
                
            }
        }).start();
    }
}
