package com.lizxing.seckilldemo.service;

/**
 * @author lizxing
 * @date 2020/11/4
 */
public interface PayService {
    void pay(String orderId);

    int checkPay(String orderId);
}
