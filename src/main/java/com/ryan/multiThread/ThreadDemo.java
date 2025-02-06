package com.ryan.multiThread;

public class ThreadDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            System.out.println("线程");
        });
        thread.start();
    }
}