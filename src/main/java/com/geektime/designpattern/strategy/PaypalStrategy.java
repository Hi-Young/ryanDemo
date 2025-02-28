package com.geektime.designpattern.strategy;

public class PaypalStrategy implements PaymentStrategy {
    private String email;
    
    public PaypalStrategy(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(int amount) {
        System.out.println("使用 PayPal（邮箱：" + email + "）支付 " + amount + " 元");
    }
}