//package com.geektime.middleware.mq;
//
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
//import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import william.rmq.common.constant.RocketMQConstant;
//
//import javax.annotation.PostConstruct;
//
///**
//* @Auther: ZhangShenao
//* @Date: 2018/9/11 17:53
//* @Description:顺序消息的消费者
//*/
//@Service
//public class OrderMessageConsumer {
//   @Value("${spring.rocketmq.namesrvAddr}")
//   private String namesrvAddr;
//
//   private final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("DefaultConsumer");
//
//   @PostConstruct
//   public void start() {
//       try {
//           //设置namesrv地址
//           consumer.setNamesrvAddr(namesrvAddr);
//
//           //从消息队列头部开始消费
//           consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
//
//           //集群消费模式
//           consumer.setMessageModel(MessageModel.CLUSTERING);
//
//           //订阅主题
//           consumer.subscribe(RocketMQConstant.TEST_TOPIC_NAME, "*");
//
//           //注册消息监听器，这里因为要实现顺序消费，所以必须注册MessageListenerOrderly
//           consumer.registerMessageListener(new OrderMessageListener());
//
//           //启动消费端
//           consumer.start();
//
//           System.err.println("Order Message Consumer Start...");
//       } catch (Exception e) {
//           throw new RuntimeException(e);
//       }
//
//   }
//}

