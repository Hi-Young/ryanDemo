package com.geektime.concurrent.basic;

public class VolatileVisibilityDemo {
    
    private static boolean flag = false;
    private static volatile boolean volatileFlag = false;
    
    public static void main(String[] args) throws InterruptedException {
        // 先让JVM预热，增加JIT优化的概率
        for (int i = 0; i < 5; i++) {
            warmup();
        }
        
        System.out.println("\n开始正式测试：");
        demonstrateWithoutVolatile();
        System.out.println("================");
//        demonstrateWithVolatile();
    }
    
    private static void warmup() throws InterruptedException {
        boolean temp = false;
        Thread t = new Thread(() -> {
            while (!temp) { }
        });
        t.start();
        Thread.sleep(100);
        t.stop();
    }
    
    private static void demonstrateWithoutVolatile() throws InterruptedException {
        flag = false;
        
        Thread threadA = new Thread(() -> {
            System.out.println("线程A启动，开始监听flag变化...");
            while (!flag) {
                // 纯空循环，最容易被优化
            }
            System.out.println("线程A检测到flag=true，退出循环");
        });
        
        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("线程B将flag设置为true");
                flag = true;
                System.out.println("线程B已修改flag=true");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        threadA.start();
        threadB.start();
        
        Thread.sleep(3000);
        if (threadA.isAlive()) {
            System.out.println("⚠️ 线程A仍在运行 - 可见性问题出现了！");
            threadA.stop();
        }
    }
    
    private static void demonstrateWithVolatile() throws InterruptedException {
        volatileFlag = false;
        
        Thread threadA = new Thread(() -> {
            System.out.println("线程A启动，开始监听volatileFlag变化...");
            while (!volatileFlag) {
                // 同样的空循环
            }
            System.out.println("线程A检测到volatileFlag=true，退出循环");
        });
        
        Thread threadB = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("线程B将volatileFlag设置为true");
                volatileFlag = true;
                System.out.println("线程B已修改volatileFlag=true");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        threadA.start();
        threadB.start();
        
        threadA.join(3000);
        if (!threadA.isAlive()) {
            System.out.println("✓ 线程A正常退出 - volatile保证了可见性");
        }
    }
}