package com.ryan.business.rocketmq.consumer;

import com.ryan.business.rocketmq.model.RocketMqDemoMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 演示消费者。
 *
 * @author codex
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${demo.rocketmq.topic}",
        consumerGroup = "${demo.rocketmq.consumer-group}",
        selectorExpression = "*",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        maxReconsumeTimes = 2
)
public class RocketMqDemoConsumer implements RocketMQListener<RocketMqDemoMessage> {

    @Override
    public void onMessage(RocketMqDemoMessage message) {
        log.info("RocketMQ receive message, messageId={}, body={}, forceDlq={}",
                message.getMessageId(), message.getBody(), message.getForceDlq());
        if (Boolean.TRUE.equals(message.getForceDlq())) {
            // 故意抛错触发重试，超过 maxReconsumeTimes 后会进入 %DLQ%<consumerGroup>。
            throw new IllegalStateException("模拟消费失败，进入死信队列");
        }
    }
}
