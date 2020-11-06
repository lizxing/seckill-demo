package com.lizxing.seckilldemo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lizxing
 * @date 2020/11/4
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.redisson.address}")
    private String addressUrl;

    @Bean
    public RedissonClient getRedisson() throws Exception{
        RedissonClient redisson = null;
        Config config = new Config();
        config.useSingleServer()
                .setAddress(addressUrl);
        redisson = Redisson.create(config);

        System.out.println(redisson.getConfig().toJSON().toString());
        return redisson;
    }
}
