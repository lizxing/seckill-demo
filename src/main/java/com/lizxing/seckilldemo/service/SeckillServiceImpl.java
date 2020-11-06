package com.lizxing.seckilldemo.service;

import com.lizxing.seckilldemo.entity.Order;
import com.lizxing.seckilldemo.mapper.GoodsMapper;
import com.lizxing.seckilldemo.mapper.OrderMapper;
import com.lizxing.seckilldemo.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author lizxing
 * @date 2020/11/3
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Value("${spring.rabbitmq.exchange.order}")
    private String orderExchangeName;

    @Value("${spring.rabbitmq.routingKey.order}")
    private String orderRoutingKey;

    @Value("${spring.rabbitmq.exchange.pay}")
    private String payExchangeName;

    @Value("${spring.rabbitmq.routingKey.pay}")
    private String payRoutingKey;

    @Override
    public boolean isAlreadySeckill(String userId, String goodsId){
        // 以userId和goodsId保证唯一
        String key = userId + "-" + goodsId;
        RLock lock = redissonClient.getLock(key);
        try {
            lock.lock();
            // set进缓存成功，代表没参加过
            return !redisUtil.setnx(key, "1", 100);
        } catch (Exception e){
            log.error("系统错误：{}", e.getMessage());
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String startSeckill(String userId, String goodsId){
        // 创建订单
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setStatus(0);
        // 生产抢单消息
        rabbitTemplate.convertAndSend(orderExchangeName, orderRoutingKey, order);
        return order.getId();
    }


    @Override
    public int order(Order order) {
        int updates = goodsMapper.reduceStock(order.getGoodsId());
        if (updates > 0) {
            // 库存-1成功
            orderMapper.insert(order);
            // 生产支付消息（该消息会延时一段时间才会被消费者监听到，消费消息时检验在这段时间内是否已支付）
            rabbitTemplate.convertAndSend(payExchangeName, payRoutingKey, order);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void pay(String orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(1);
        orderMapper.updateById(order);
    }


}
