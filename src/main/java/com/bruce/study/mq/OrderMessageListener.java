package com.bruce.study.mq;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.util.CollectionUtils;
import java.util.List;

/**
* @Auther: ZhangShenao
* @Date: 2018/9/11 17:53
* @Description:顺序消息监听器
*/
public class OrderMessageListener implements MessageListenerOrderly{
   @Override
   public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
       if (CollectionUtils.isEmpty(msgs)){
           return ConsumeOrderlyStatus.SUCCESS;
       }
       //设置自动提交
       context.setAutoCommit(true);
       msgs.stream()
               .forEach(msg -> {
                   try {
                       String messageBody = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                       System.err.println("Handle Order Message: messageId: " + msg.getMsgId() + ",topic: " + msg.getTopic() + ",tags: "
                               + msg.getTags() + ",keys: " + msg.getKeys() + ",messageBody: " + messageBody);
                   } catch (Exception e) {
                       throw new RuntimeException(e);
                   }
               });
       return ConsumeOrderlyStatus.SUCCESS;
   }
}
