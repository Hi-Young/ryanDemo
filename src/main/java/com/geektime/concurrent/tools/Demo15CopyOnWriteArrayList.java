package com.geektime.concurrent.basic;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo15CopyOnWriteArrayList {
    public static void main(String[] args) {
        // 1. 初始化 CopyOnWriteArrayList
        List<Integer> tempList = Arrays.asList(1, 2);
        CopyOnWriteArrayList<Integer> copyList = new CopyOnWriteArrayList<>(tempList);

        // 2. 使用线程池模拟多线程对列表的读写
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(new ReadThread(copyList));
        executorService.execute(new WriteThread(copyList));
        executorService.execute(new WriteThread(copyList));
        executorService.execute(new WriteThread(copyList));
        executorService.execute(new ReadThread(copyList));
        executorService.execute(new WriteThread(copyList));
        executorService.execute(new ReadThread(copyList));
        executorService.execute(new WriteThread(copyList));

        // 等待一段时间，让线程运行结束
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final copyList size: " + copyList.size());
        System.out.println("Final copyList content: " + copyList);
        executorService.shutdown();
    }
}

class ReadThread implements Runnable {
    private List<Integer> list;

    public ReadThread(List<Integer> list) {
        this.list = list;
    }

    @Override
    public void run() {
        // 使用格式化输出，显示线程名称、列表大小和当前快照
        System.out.printf("[%s] Snapshot -> size: %d, elements: %s%n",
                Thread.currentThread().getName(),
                list.size(),
                list);
    }
}

class WriteThread implements Runnable {
    private List<Integer> list;

    public WriteThread(List<Integer> list) {
        this.list = list;
    }

    @Override
    public void run() {
        // 添加元素后也打印输出，显示线程名称和写入信息
        list.add(9);
        System.out.printf("[%s] Added element: 9%n", Thread.currentThread().getName());
    }
}

