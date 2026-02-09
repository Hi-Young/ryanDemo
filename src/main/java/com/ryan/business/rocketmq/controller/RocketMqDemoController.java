package com.ryan.business.rocketmq.controller;

import com.ryan.business.rocketmq.service.RocketMqDemoProducer;
import com.ryan.common.base.ResultVO;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RocketMQ 演示接口。
 *
 * @author codex
 */
@RestController
@RequestMapping("/rocketmq/demo")
public class RocketMqDemoController {

    @Resource
    private RocketMqDemoProducer rocketMqDemoProducer;

    @Value("${demo.rocketmq.topic}")
    private String topic;

    @Value("${demo.rocketmq.tag}")
    private String tag;

    @Value("${demo.rocketmq.consumer-group}")
    private String consumerGroup;

    @Value("${demo.rocketmq.dashboard-url}")
    private String dashboardUrl;

    @PostMapping("/send")
    public ResultVO<Map<String, Object>> send(@RequestParam(defaultValue = "hello rocketmq") String body) {
        SendResult sendResult = rocketMqDemoProducer.sendNormalMessage(body);
        return ResultVO.success(buildSendResult(sendResult, body, false));
    }

    @PostMapping("/send-dlq")
    public ResultVO<Map<String, Object>> sendToDlq(@RequestParam(defaultValue = "dead-letter-demo") String body) {
        SendResult sendResult = rocketMqDemoProducer.sendForceDlqMessage(body);
        return ResultVO.success(buildSendResult(sendResult, body, true));
    }

    @GetMapping("/meta")
    public ResultVO<Map<String, Object>> meta() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("topic", topic);
        result.put("tag", tag);
        result.put("consumerGroup", consumerGroup);
        result.put("retryTopic", "%RETRY%" + consumerGroup);
        result.put("deadLetterTopic", "%DLQ%" + consumerGroup);
        result.put("dashboard", dashboardUrl);
        return ResultVO.success(result);
    }

    private Map<String, Object> buildSendResult(SendResult sendResult, String body, boolean forceDlq) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("topic", topic);
        result.put("tag", tag);
        result.put("body", body);
        result.put("forceDlq", forceDlq);
        result.put("sendStatus", sendResult.getSendStatus());
        result.put("msgId", sendResult.getMsgId());
        result.put("offsetMsgId", sendResult.getOffsetMsgId());
        result.put("queueId", sendResult.getMessageQueue().getQueueId());
        return result;
    }
}
