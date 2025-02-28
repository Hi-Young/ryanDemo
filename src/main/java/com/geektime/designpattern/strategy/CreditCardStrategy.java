package com.geektime.designpattern.strategy;

public class CreditCardStrategy implements PaymentStrategy {
    private String cardNumber;
    
    public CreditCardStrategy(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void pay(int amount) {
        System.out.println("使用信用卡 " + cardNumber + " 支付 " + amount + " 元");
    }
}