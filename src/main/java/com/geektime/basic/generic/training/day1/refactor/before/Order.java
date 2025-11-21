package com.geektime.basic.generic.training.day1.refactor.before;

/**
 * 订单实体（临时定义）
 */
public class Order {
    private String orderId;
    private String customerName;

    public Order(String orderId, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "Order{" + "orderId='" + orderId + '\'' + ", customerName='" + customerName + '\'' + '}';
    }
}