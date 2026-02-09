package com.ryan.business.rocketmq.service;

import com.ryan.business.rocketmq.model.RocketMqDemoMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import javax.annotation.Resource;

/**
 * RocketMQ 演示生产者。
 *
 * @author codex
 */
@Slf4j
@Service
public class RocketMqDemoProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Value("${demo.rocketmq.topic}")
    private String topic;

    @Value("${demo.rocketmq.tag}")
    private String tag;

    public SendResult sendNormalMessage(String body) {
        RocketMqDemoMessage message = RocketMqDemoMessage.normal(body);
        return send(message);
    }

    public SendResult sendForceDlqMessage(String body) {
        RocketMqDemoMessage message = RocketMqDemoMessage.forceDlq(body);
        return send(message);
    }

    private SendResult send(RocketMqDemoMessage message) {
        String destination = topic + ":" + tag;
        SendResult sendResult = rocketMQTemplate.syncSend(destination, message);
        log.info("RocketMQ send success, destination={}, messageId={}, sendResult={}",
                destination, message.getMessageId(), sendResult);
        return sendResult;
    }
}
