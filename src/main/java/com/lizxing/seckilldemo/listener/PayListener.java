package com.lizxing.seckilldemo.listener;

import com.alibaba.fastjson.JSONObject;
import com.lizxing.seckilldemo.service.PayService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消费支付消息
 * @author lizxing
 * @date 2020/11/4
 */
@Slf4j
@Component
public class PayListener  implements ChannelAwareMessageListener {

    @Autowired
    PayService payService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            Long tag = message.getMessageProperties().getDeliveryTag();
            String str = new String(message.getBody(), "utf-8");
            log.info("消费者接收到的消息：{}",str);
            JSONObject obj = JSONObject.parseObject(str);
            String orderId = obj.getString("id");
            // 确认该订单是否付款
            if (payService.checkPay(orderId) == 1){
                log.info("交易成功，订单id：{}", orderId);
            } else {
                log.info("交易失败，订单id：{}未支付", orderId);
            }
            // 无论是否支付，都消费此消息
            channel.basicAck(tag, true);
        }catch(Exception e){
            log.error("消费者消费消息发生异常：{}",e.getMessage());
        }
    }
}
