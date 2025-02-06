package com.geektime.multithread;

public class ThreadDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            System.out.println("线程");
        });
        thread.start();
    }
}
