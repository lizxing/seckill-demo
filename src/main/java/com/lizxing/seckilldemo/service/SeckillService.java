package com.lizxing.seckilldemo.service;


import com.lizxing.seckilldemo.entity.Order;

/**
 * @author lizxing
 * @date 2020/11/3
 */
public interface SeckillService {

    boolean isAlreadySeckill(String userId, String goodsId);

    String startSeckill(String userId, String goodsId);

    int order(Order order);

    void pay(String orderId);
}
