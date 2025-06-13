package com.geektime.concurrent.basic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author heyyon
 * @date 2025-02-06 21:38
 */
public class Demo10ReentrantReadWriteLock {
    private static volatile int count = 0;

    public static void main(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        WriteDemo writeDemo = new WriteDemo(lock);
        ReadDemo readDemo = new ReadDemo(lock);
        for (int i = 0; i < 3; i++) {
            new Thread(writeDemo).start();
        }
        for (int i = 0; i < 5; i++) {
            new Thread(readDemo).start();
        }
    }

    static class WriteDemo implements Runnable {
        ReentrantReadWriteLock lock;

        public WriteDemo(ReentrantReadWriteLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.writeLock().lock();
                count++;
                System.out.println("写锁：" + count);
                lock.writeLock().unlock();
            }
        }
    }

    static class ReadDemo implements Runnable {
        ReentrantReadWriteLock lock;

        public ReadDemo(ReentrantReadWriteLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.readLock().lock();
                System.out.println("读锁：" + count);
                lock.readLock().unlock();
            }
        }
    }
}

