package com.geektime.framework.spring.ioc;

public class SynchronizedTest {
    private int count = 0;

    private boolean produced = false;

    private  Object object = new Object();

    public void produce() throws InterruptedException {
        synchronized (object) {
            System.out.println("生产线程启动");
            while (produced) {
                object.wait();
            }
            System.out.println(count + " :produced");
            produced = true;
            object.notify();
        }
    }

    public void consume() throws InterruptedException {
        synchronized (object) {
            System.out.println("消费线程启动");
            while (!produced) {
                object.wait();
            }
            System.out.println(count + " :consumed");
            produced = false;
            count++;
            object.notify();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedTest synchronizedTest = new SynchronizedTest();
        new Thread((() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    synchronizedTest.produce();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        )).start();

        new Thread((() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    synchronizedTest.consume();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        )).start();
    }
}

