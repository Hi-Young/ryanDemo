package com.geektime.multithread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author heyyon
 * @date 2025-02-06 22:04
 */
public class Demo11CountDownLatch {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(()->{
                try { TimeUnit.SECONDS.sleep(5); } catch
                (InterruptedException e) {e.printStackTrace(); }
                System.out.println(Thread.currentThread().getName() + "\t上完班，离开公司");
                        countDownLatch.countDown();
            }, String.valueOf(i)).start();
        }
        new Thread(()->{
            try {
                countDownLatch.await();//卷王也是有极限的，设置超时时间
                System.out.println(Thread.currentThread().getName()+"\t卷王最 后关灯走人");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "7").start();
    }
}
