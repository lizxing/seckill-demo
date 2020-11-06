package com.lizxing.seckilldemo.listener;

import com.alibaba.fastjson.JSONObject;
import com.lizxing.seckilldemo.entity.Order;
import com.lizxing.seckilldemo.service.SeckillService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消费抢单消息
 * @author lizxing
 * @date 2020/11/3
 */
@Slf4j
@Component
public class OrderListener implements ChannelAwareMessageListener {

    @Autowired
    SeckillService seckillService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try{
            // 获取tag
            long tag = message.getMessageProperties().getDeliveryTag();
            String str = new String(message.getBody(),"utf-8");
            log.info("消费者接收到的消息：{}",str);
            JSONObject obj = JSONObject.parseObject(str);
            Order order = JSONObject.toJavaObject(obj, Order.class);
            // 下单
            if (seckillService.order(order) == 1){
                log.info("下单成功，订单id：{}", order.getId());
            } else {
                log.info("下单失败，库存不足");
            }
            // 消费确认
            channel.basicAck(tag,true);
        }catch(Exception e){
            log.error("消费者消费消息发生异常：{}",e.getMessage());
        }
    }
}
