package com.geektime.concurrent.lock;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

class Node {
    // 标记当前节点是否持有锁（true 表示等待持有锁）
    volatile boolean locked = true;
    // 如果使用 park/unpark，则记录对应线程
    Thread thread;
    // 为了支持 unpark，需要保存后继节点的引用（非纯粹的 CLH，但常见于实际变体）
    volatile Node next;
}

class CLHLock {
    // 使用原子引用管理队列尾部，初始为一个“哑节点”
    AtomicReference<Node> tail = new AtomicReference<>(new Node());

    // 每个线程都会维护自己的节点
    ThreadLocal<Node> currentNode = ThreadLocal.withInitial(() -> {
        Node node = new Node();
        node.thread = Thread.currentThread();
        return node;
    });

    public void lock() {
        Node node = currentNode.get();
        // 保证每次进入锁时节点状态为等待锁
        node.locked = true;
        node.next = null;

        // 将自己的节点插入到队列尾部，同时获得前驱节点
        Node pred = tail.getAndSet(node);
        // 如果需要，可以将前驱节点的 next 指针指向当前节点
        pred.next = node;

        // 自旋等待前驱节点释放锁
        while (pred.locked) {
            // 可以采用 busy-wait 自旋，或者在自旋一定次数后使用 park 来降低 CPU 消耗
            // 例如：
            // if (shouldPark()) {
            //     LockSupport.park();
            // }
        }
        // 到这里说明前驱节点已经释放锁，可以继续进入临界区
    }

    public void unlock() {
        Node node = currentNode.get();
        // 释放锁：将状态置为 false
        node.locked = false;
        // 如果后继节点因等待而被 park，则主动唤醒它
        if (node.next != null) {
            LockSupport.unpark(node.next.thread);
        }
        // 另外，为了避免持有 stale 的节点，可以让当前线程重新创建一个新的 Node，
        // 或在下一次 lock() 时重新初始化 currentNode
    }
}

