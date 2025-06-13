package com.geektime.designpattern.creational.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付工厂模式示例
 * 直接复制整个文件到IDEA中即可运行
 */
public class PaymentFactoryExample {

    public static void main(String[] args) {
        // 创建订单
        Order order = new Order("ORD-" + System.currentTimeMillis(), 199.99, "USD");
        System.out.println("订单创建成功: " + order);

        // 获取支付处理器
        PaymentProcessor creditCardProcessor = PaymentFactory.getProcessor("CREDIT_CARD");
        PaymentProcessor alipayProcessor = PaymentFactory.getProcessor("ALIPAY");
        PaymentProcessor bankTransferProcessor = PaymentFactory.getProcessor("BANK_TRANSFER");

        // 使用信用卡处理器
        System.out.println("\n===== 使用信用卡支付 =====");
        PaymentResult ccResult = creditCardProcessor.processPayment(order);
        System.out.println("支付结果: " + ccResult);

        // 使用支付宝处理器
        System.out.println("\n===== 使用支付宝支付 =====");
        PaymentResult alipayResult = alipayProcessor.processPayment(order);
        System.out.println("支付结果: " + alipayResult);

        // 使用银行转账处理器
        System.out.println("\n===== 使用银行转账支付 =====");
        PaymentResult bankResult = bankTransferProcessor.processPayment(order);
        System.out.println("支付结果: " + bankResult);

        // 测试退款功能
        System.out.println("\n===== 测试退款功能 =====");
        if (ccResult.isSuccess() && creditCardProcessor.supportsRefund()) {
            PaymentResult refundResult = creditCardProcessor.refundPayment(ccResult.getTransactionId(), order.getTotalAmount());
            System.out.println("信用卡退款结果: " + refundResult);
        }

        try {
            if (bankResult.isSuccess() && bankTransferProcessor.supportsRefund()) {
                bankTransferProcessor.refundPayment(bankResult.getTransactionId(), order.getTotalAmount());
            } else {
                System.out.println("银行转账不支持自动退款");
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("异常: " + e.getMessage());
        }

        // 测试未知支付方式
        System.out.println("\n===== 测试未知支付方式 =====");
        try {
            PaymentProcessor unknownProcessor = PaymentFactory.getProcessor("BITCOIN");
        } catch (IllegalArgumentException e) {
            System.out.println("异常: " + e.getMessage());
        }

        // 添加新的支付处理器
        System.out.println("\n===== 动态添加新支付方式 =====");
        PaymentFactory.registerProcessor("PAYPAL", new PayPalProcessor());
        PaymentProcessor paypalProcessor = PaymentFactory.getProcessor("PAYPAL");
        PaymentResult paypalResult = paypalProcessor.processPayment(order);
        System.out.println("PayPal支付结果: " + paypalResult);
    }

    // ========== 支付结果类 ==========
    static class PaymentResult {
        private boolean success;
        private String transactionId;
        private String message;

        public PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "PaymentResult{" +
                    "success=" + success +
                    ", transactionId='" + transactionId + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    // ========== 订单类 ==========
    static class Order {
        private String orderId;
        private double totalAmount;
        private String currency;

        public Order(String orderId, double totalAmount, String currency) {
            this.orderId = orderId;
            this.totalAmount = totalAmount;
            this.currency = currency;
        }

        public String getOrderId() {
            return orderId;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public String getCurrency() {
            return currency;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "orderId='" + orderId + '\'' +
                    ", totalAmount=" + totalAmount +
                    ", currency='" + currency + '\'' +
                    '}';
        }
    }
    
    // ========== 支付处理器接口 ==========
    interface PaymentProcessor {
        PaymentResult processPayment(Order order);

        boolean supportsRefund();

        PaymentResult refundPayment(String transactionId, double amount);

        String getPaymentGatewayName();
    }

    // ========== 具体支付处理器实现 ==========

    // 信用卡支付
    static class CreditCardProcessor implements PaymentProcessor {
        @Override
        public PaymentResult processPayment(Order order) {
            System.out.println("通过信用卡处理订单支付: " + order.getOrderId());
            System.out.println("连接到信用卡网关处理 $" + order.getTotalAmount());

            // 模拟支付处理
            boolean paymentSuccessful = Math.random() > 0.1; // 90%成功率

            if (paymentSuccessful) {
                String txId = "CC-TX-" + UUID.randomUUID().toString().substring(0, 8);
                return new PaymentResult(true, txId, "信用卡支付成功");
            } else {
                return new PaymentResult(false, null, "信用卡被拒绝");
            }
        }

        @Override
        public boolean supportsRefund() {
            return true;
        }

        @Override
        public PaymentResult refundPayment(String transactionId, double amount) {
            System.out.println("处理信用卡退款: " + transactionId + ", 金额: $" + amount);
            return new PaymentResult(true, "RF-" + transactionId, "退款成功");
        }

        @Override
        public String getPaymentGatewayName() {
            return "Stripe";
        }
    }

    // 支付宝
    static class AlipayProcessor implements PaymentProcessor {
        @Override
        public PaymentResult processPayment(Order order) {
            System.out.println("通过支付宝处理订单支付: " + order.getOrderId());
            System.out.println("生成支付宝支付链接...");

            // 模拟支付处理
            String paymentUrl = "https://alipay.com/pay/" + order.getOrderId();
            System.out.println("支付链接: " + paymentUrl);

            String txId = "ALI-TX-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txId, "请使用支付宝扫码完成支付");
        }

        @Override
        public boolean supportsRefund() {
            return true;
        }

        @Override
        public PaymentResult refundPayment(String transactionId, double amount) {
            System.out.println("处理支付宝退款: " + transactionId + ", 金额: $" + amount);
            return new PaymentResult(true, "ALI-RF-" + transactionId, "退款已提交");
        }

        @Override
        public String getPaymentGatewayName() {
            return "Alipay";
        }
    }

    // 银行转账
    static class BankTransferProcessor implements PaymentProcessor {
        @Override
        public PaymentResult processPayment(Order order) {
            System.out.println("通过银行转账处理订单支付: " + order.getOrderId());
            System.out.println("生成银行转账信息...");

            System.out.println("收款账户: BANK-12345678");
            System.out.println("收款人: Example Company");
            System.out.println("金额: $" + order.getTotalAmount());
            System.out.println("备注: " + order.getOrderId());

            String txId = "BT-PENDING-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txId, "请在24小时内完成银行转账");
        }

        @Override
        public boolean supportsRefund() {
            return false;
        }

        @Override
        public PaymentResult refundPayment(String transactionId, double amount) {
            throw new UnsupportedOperationException("银行转账不支持自动退款，请联系客服");
        }

        @Override
        public String getPaymentGatewayName() {
            return "Manual Bank Transfer";
        }
    }

    // PayPal支付
    static class PayPalProcessor implements PaymentProcessor {
        @Override
        public PaymentResult processPayment(Order order) {
            System.out.println("通过PayPal处理订单支付: " + order.getOrderId());
            System.out.println("重定向至PayPal支付页面...");

            // 模拟支付处理
            String paymentUrl = "https://paypal.com/checkout?orderId=" + order.getOrderId();
            System.out.println("PayPal链接: " + paymentUrl);

            String txId = "PP-TX-" + UUID.randomUUID().toString().substring(0, 8);
            return new PaymentResult(true, txId, "PayPal支付已初始化");
        }

        @Override
        public boolean supportsRefund() {
            return true;
        }

        @Override
        public PaymentResult refundPayment(String transactionId, double amount) {
            System.out.println("处理PayPal退款: " + transactionId + ", 金额: $" + amount);
            return new PaymentResult(true, "PP-RF-" + transactionId, "PayPal退款已处理");
        }

        @Override
        public String getPaymentGatewayName() {
            return "PayPal";
        }
    }

    // ========== 支付处理器工厂 ==========
    static class PaymentFactory {
        private static Map<String, PaymentProcessor> processors = new HashMap<>();

        // 初始化默认支付处理器
        static {
            processors.put("CREDIT_CARD", new CreditCardProcessor());
            processors.put("ALIPAY", new AlipayProcessor());
            processors.put("BANK_TRANSFER", new BankTransferProcessor());
        }

        // 注册新的支付处理器
        public static void registerProcessor(String method, PaymentProcessor processor) {
            processors.put(method, processor);
            System.out.println("已注册新的支付处理器: " + method + " (" + processor.getPaymentGatewayName() + ")");
        }

        // 获取支付处理器
        public static PaymentProcessor getProcessor(String method) {
            PaymentProcessor processor = processors.get(method);
            if (processor == null) {
                throw new IllegalArgumentException("不支持的支付方式: " + method);
            }
            return processor;
        }
    }
}
