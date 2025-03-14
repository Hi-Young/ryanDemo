package com.bruce.study.thread;

class ThreadA extends Thread{
    public ThreadA(String name) {
        super(name);
    }
    @Override
    public void run() {
        synchronized (this) {
            try {                       
                Thread.sleep(5000); //  使当前线阻塞 1 s，确保主程序的 t1.wait(); 执行之后再执行 notify()
            } catch (Exception e) {
                e.printStackTrace();
            }           
            System.out.println(Thread.currentThread().getName()+" call notify()");
            // 唤醒当前的wait线程
            this.notify();
        }
    }
}
