package com.geektime.designpattern.behavioral.strategy;

// 策略接口：定义支付算法
public interface PaymentStrategy {
    void pay(int amount);
}

// 具体策略：信用卡支付


// 具体策略：PayPal支付


// 上下文类：持有一个 PaymentStrategy 的引用，根据需求调用对应策略


// 测试策略模式的主类


