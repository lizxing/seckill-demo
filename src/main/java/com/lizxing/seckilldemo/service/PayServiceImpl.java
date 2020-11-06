package com.lizxing.seckilldemo.service;

import com.lizxing.seckilldemo.entity.Order;
import com.lizxing.seckilldemo.mapper.GoodsMapper;
import com.lizxing.seckilldemo.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lizxing
 * @date 2020/11/4
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PayServiceImpl implements PayService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Override
    public void pay(String orderId){
        orderMapper.updateOrderStatus(orderId);
        log.info("订单：{}完成支付" ,orderId);
    }

    @Override
    public int checkPay(String orderId){
        Order order = orderMapper.selectById(orderId);
        if (order.getStatus() != 1) {
            // 库存 + 1
            goodsMapper.addStock(order.getGoodsId());
            return 0;
        } else {
            return 1;
        }
    }
}
