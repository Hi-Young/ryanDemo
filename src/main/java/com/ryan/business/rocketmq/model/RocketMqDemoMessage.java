package com.ryan.business.rocketmq.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * RocketMQ 演示消息体。
 *
 * @author codex
 */
@Data
public class RocketMqDemoMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String messageId;
    private String body;
    private Boolean forceDlq;
    private String sendTime;

    public static RocketMqDemoMessage normal(String body) {
        return build(body, false);
    }

    public static RocketMqDemoMessage forceDlq(String body) {
        return build(body, true);
    }

    private static RocketMqDemoMessage build(String body, boolean forceDlq) {
        RocketMqDemoMessage message = new RocketMqDemoMessage();
        message.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        message.setBody(body);
        message.setForceDlq(forceDlq);
        message.setSendTime(LocalDateTime.now().format(FORMATTER));
        return message;
    }
}
