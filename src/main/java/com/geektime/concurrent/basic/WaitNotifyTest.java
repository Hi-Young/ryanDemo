package com.geektime.concurrent.basic;

public class WaitNotifyTest {
    static Object o = new Object();
    static boolean flag = false;

    public static void main(String[] args) {

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                synchronized (o) {
                    System.out.println("start1:" + i);

                    while (flag) {

                        try {
                            o.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("生产者生产一个内容");
                    flag = true;
                    o.notify();
                }
            }
        }).start();

        new Thread(() -> {

            for (int i = 0; i < 10; i++) {
                synchronized (o) {
                    while (!flag) {
                        System.out.println("start2:" + i);


                        try {
                            o.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("消费者消费一个内容");
                    flag = false;
                    o.notify();

                }
            }
        }).start();

    }
}

