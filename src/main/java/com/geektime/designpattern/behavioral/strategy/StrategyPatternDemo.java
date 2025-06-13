package com.geektime.designpattern.behavioral.strategy;

public class StrategyPatternDemo {
    public static void main(String[] args) {
        // 初始策略：使用信用卡支付
        PaymentContext context = new PaymentContext(new CreditCardStrategy("1234-5678-9012-3456"));
        context.executePayment(100);
        
        // 切换策略：使用 PayPal 支付
        context.setPaymentStrategy(new PaypalStrategy("user@example.com"));
        context.executePayment(200);
    }
}
