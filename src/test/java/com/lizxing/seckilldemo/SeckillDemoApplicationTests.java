package com.lizxing.seckilldemo;

import com.lizxing.seckilldemo.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeckillDemoApplicationTests {

    @Autowired
    Redisson redisson;

    @Autowired
    RedisUtil redisUtil;

    @Test
    void contextLoads() {
    }

    @Test
    void test(){
        String testKey = "testKey";
        RLock lock = redisson.getLock(testKey);
        try {
            System.out.println("加锁");
            lock.lock();
            boolean flag = redisUtil.setnx(testKey, "1", 100);
            if (flag){
                System.out.println("set进缓存");
            } else {
                System.out.println("已存在");
            }
        } catch (Exception e){
            System.out.println("错误:" + e.getMessage());
        } finally {
            System.out.println("解锁");
            lock.unlock();
        }
    }

}
