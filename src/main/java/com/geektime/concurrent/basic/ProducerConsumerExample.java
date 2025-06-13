package com.geektime.concurrent.basic;

public class ProducerConsumerExample {
    private static final Object lock = new Object();  // 锁对象
    private static int data = 0;  // 数据
    private static boolean isProduced = false;  // 标记数据是否已生产

    // 生产者线程
    static class Producer extends Thread {
        @Override
        public void run() {
            synchronized (lock) {
                try {
                    while (isProduced) {
                        // 如果数据已生产，则生产者线程等待
                        lock.wait();
                    }
                    // 生产数据
                    data++;
                    System.out.println("Produced: " + data);
                    isProduced = true;  // 标记数据已生产
                    // 通知消费者线程可以消费数据
                    lock.notify();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 消费者线程
    static class Consumer extends Thread {
        @Override
        public void run() {
            synchronized (lock) {
                try {
                    while (!isProduced) {
                        // 如果数据未生产，消费者线程等待
                        lock.wait();
                    }
                    // 消费数据
                    System.out.println("Consumed: " + data);
                    isProduced = false;  // 标记数据已消费
                    // 通知生产者线程可以生产新的数据
                    lock.notify();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 创建生产者和消费者线程
        Thread producer = new Producer();
        Thread consumer = new Consumer();

        // 启动线程
        producer.start();
        consumer.start();

        // 等待线程结束
        producer.join();
        consumer.join();
    }
}

