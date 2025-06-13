package com.geektime.concurrent.lock;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class SimpleCustomLock {
    private static class Sync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire(int acquires) {
            if (compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int releases) {
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        // 添加辅助方法来查看队列状态
        public String getQueuedThreadsInfo() {
            StringBuilder sb = new StringBuilder();
            for (Thread t : getQueuedThreads()) {
                sb.append(t.getName()).append(", ");
            }
            return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "empty";
        }
    }

    private final Sync sync = new Sync();

    public void lock() {
        sync.acquire(1);
    }

    public void unlock() {
        sync.release(1);
    }

    // 添加方法来获取队列信息
    public String getQueueInfo() {
        return "Queue length: " + sync.getQueueLength() + 
               ", Queued threads: " + sync.getQueuedThreadsInfo();
    }
}
