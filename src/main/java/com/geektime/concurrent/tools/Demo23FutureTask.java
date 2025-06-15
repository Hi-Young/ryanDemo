package com.geektime.concurrent.tools;


import java.util.concurrent.*;

public class Demo23FutureTask {
    public static void main(String[] args) {
        Task1 task = new Task1();
//FutureTask继承Future和Runnalbe接口
        FutureTask<Integer> integerFutureTask = new FutureTask<>(task);
// new Thread(integerFutureTask).start();
        ExecutorService service = Executors.newCachedThreadPool();
        Future<?> submit = service.submit(integerFutureTask);
        try {
            System.out.println("task运行结果：" + integerFutureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

class Task1 implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("子线程正在计算");
        Thread.sleep(3000);
//模拟子线程处理业务逻辑
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i;
        }
        return sum;
    }
}
