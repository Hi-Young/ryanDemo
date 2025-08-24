package com.arthas.demo;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class ArthasThreadDemo {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public void startCpuIntensiveThread() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("cpu-intensive-thread");
                int count = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    count += Math.sin(count) * Math.cos(count);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("CPU intensive thread finished.");
            }
        };
        executor.submit(task);
    }

    public void startWaitingThread() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("waiting-thread");
                try {
                    Thread.sleep(300 * 1000); // Wait for 5 minutes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        executor.submit(task);
    }

    public void startDeadlockThreads() {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    System.out.println("DeadlockThread-1 acquired lock1");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    synchronized (lock2) {
                        System.out.println("DeadlockThread-1 acquired lock2");
                    }
                }
            }
        }, "DeadlockThread-1");

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock2) {
                    System.out.println("DeadlockThread-2 acquired lock2");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    synchronized (lock1) {
                        System.out.println("DeadlockThread-2 acquired lock1");
                    }
                }
            }
        }, "DeadlockThread-2");

        thread1.start();
        thread2.start();
        System.out.println("Deadlock threads started.");
    }
}
