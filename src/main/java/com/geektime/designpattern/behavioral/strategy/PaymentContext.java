package com.geektime.designpattern.behavioral.strategy;

public class PaymentContext {
    private PaymentStrategy paymentStrategy;
    
    public PaymentContext(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    // 允许在运行时切换策略
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    public void executePayment(int amount) {
        paymentStrategy.pay(amount);
    }
}
