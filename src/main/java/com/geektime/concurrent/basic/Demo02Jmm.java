package com.geektime.concurrent.basic;

public class Demo02Jmm {
    public static void main(String[] args) throws InterruptedException {
        JmmDemo demo = new JmmDemo();
        Thread t = new Thread(demo);
        Thread t1 = new Thread(demo);

//        t.start();
//        Thread.sleep(100);
//        demo.flag = false;
//        System.out.println("已经修改为false");
//        System.out.println(demo.flag);
//        synchronized (Demo02Jmm.class) {
//
//        }
    }

    static class JmmDemo implements Runnable {
        int a = 10;
        int b = 20;
        int c = 30;
        public boolean flag = true;

        public void run() {
            System.out.println("子线程执行。。。");
            while (flag) {
                System.out.println("子线程。。。");
            }
            System.out.println("子线程结束。。。");
        }
    }
}

