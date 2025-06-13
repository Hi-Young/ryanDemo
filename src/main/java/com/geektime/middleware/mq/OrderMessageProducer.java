//package com.geektime.middleware.mq;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.apache.rocketmq.common.message.Message;
//import org.apache.rocketmq.remoting.common.RemotingHelper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import william.rmq.common.constant.RocketMQConstant;
//import william.rmq.producer.common.CommonSendCallback;
//import javax.annotation.PostConstruct;
//
///**
//* @Auther: ZhangShenao
//* @Date: 2018/9/11 17:32
//* @Description:顺序消息生产端
//*/
//@Slf4j
//@Service
//public class OrderMessageProducer {
//   @Value("${spring.rocketmq.namesrvAddr}")
//   private String namesrvAddr;
//
//   private static final DefaultMQProducer producer = new DefaultMQProducer("OrderProducer");
//
//   private static final String[] ORDER_MESSAGES = {"下单","结算","支付","完成"};
//
//
//   @PostConstruct
//   public void sendMessage() {
//       try {
//           //设置namesrv
//           producer.setNamesrvAddr(namesrvAddr);
//
//           //启动Producer
//           producer.start();
//
//           System.err.println("Order Message Producer Start...");
//
//           //创建3组消息，每组消息发往同一个Queue，保证消息的局部有序性
//           String tags = "Tags";
//
//           OrderMessageQueueSelector orderMessageQueueSelector = new OrderMessageQueueSelector();
//
//           //注：要实现顺序消费，必须同步发送消息
//           for (int i = 0;i < 3;i++){
//               String orderId = "" + (i + 1);
//               for (int j = 0,size = ORDER_MESSAGES.length;j < size;j++){
//                   String message = "Order-" + orderId + "-" + ORDER_MESSAGES[j];
//                   String keys = message;
//                   byte[] messageBody = message.getBytes(RemotingHelper.DEFAULT_CHARSET);
//                   Message mqMsg = new Message(RocketMQConstant.TEST_TOPIC_NAME, tags, keys, messageBody);
//                   producer.send(mqMsg, orderMessageQueueSelector,i);
//               }
//           }
//
//
//       } catch (Exception e) {
//           log.error("Message Producer: Send Message Error ", e);
//       }
//
//   }
//}

