package com.lizxing.seckilldemo.controller;

import com.lizxing.seckilldemo.service.PayService;
import com.lizxing.seckilldemo.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lizxing
 * @date 2020/11/4
 */
@RestController
public class SeckillController {

    @Autowired
    SeckillService secKillService;

    @Autowired
    PayService payService;

    @RequestMapping("/test")
    public String test(){
        return "test";
    }

    @PostMapping("/seckill/{userId}/{goodsId}")
    public String seckill(@PathVariable("userId") String userId, @PathVariable("goodsId") String goodsId){
        // 检查该用户是否已参加过本商品的秒杀
        if (secKillService.isAlreadySeckill(userId, goodsId)){
            return "用户" + userId + "在100秒内已参与过本次秒杀";
        }
        // 开始秒杀
        String orderId = secKillService.startSeckill(userId, goodsId);
        return "抢单成功，订单id为：" + orderId + "，请在1分钟内完成支付";
    }

    @PostMapping("/pay/{orderId}")
    public String pay(@PathVariable("orderId") String orderId){
        // 模拟支付
        payService.pay(orderId);
        return null;
    }

}
