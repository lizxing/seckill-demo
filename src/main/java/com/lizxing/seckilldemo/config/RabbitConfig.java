package com.lizxing.seckilldemo.config;

import com.lizxing.seckilldemo.listener.OrderListener;
import com.lizxing.seckilldemo.listener.PayListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lizxing
 * @date 2020/11/3
 */
@Slf4j
@Configuration
public class RabbitConfig {

    /**
     * channel链接工厂
     */
    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private OrderListener orderListener;

    @Autowired
    private PayListener payListener;

    /**
     * 监听器容器配置
     */
    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    @Value("${spring.rabbitmq.exchange.order}")
    private String orderExchangeName;

    @Value("${spring.rabbitmq.queue.order}")
    private String orderQueueName;

    @Value("${spring.rabbitmq.routingKey.order}")
    private String orderRoutingKey;

    @Value("${spring.rabbitmq.exchange.pay}")
    private String payExchangeName;

    @Value("${spring.rabbitmq.exchange.deadLetter}")
    private String deadLetterExchangeName;

    @Value("${spring.rabbitmq.routingKey.pay}")
    private String payRoutingKey;

    @Value("${spring.rabbitmq.ttl}")
    private Integer ttl;

    @Value("${spring.rabbitmq.queue.deadLetter}")
    private String deadLetterQueueName;

    @Value("${spring.rabbitmq.queue.pay}")
    private String payQueueName;



    //---------------------------------------rabbitTemplate配置------------------------------------------------------

    /**
     * rabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(){
        //消息发送成功确认
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        //消息发送失败确认
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //设置消息发送格式为json
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setMandatory(true);
        //消息发送到exchange回调
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
//            }
//        });
//        //消息从exchange发送到queue失败回调
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//                log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
//            }
//        });
        return rabbitTemplate;
    }

    //---------------------------------------order相关------------------------------------------------

    /**
     * order队列
     * @return
     */
    @Bean
    public Queue orderQueue(){
        return new Queue(orderQueueName);
    }

    /**
     * order交换机
     * @return
     */
    @Bean
    public DirectExchange orderDirectExchange(){
        return new DirectExchange(orderExchangeName);
    }

    /**
     * order队列交换机绑定
     * @return
     */
    @Bean
    public Binding simpleBinding(Queue orderQueue, DirectExchange orderDirectExchange){
        return BindingBuilder.bind(orderQueue).to(orderDirectExchange).with(orderRoutingKey);
    }

    /**
     * 声明订单队列监听器配置容器
     * @return
     */
    @Bean("orderListenerContainer")
    public SimpleMessageListenerContainer orderListenerContainer(){
        //创建监听器容器工厂
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //将配置信息和链接信息赋给容器工厂
        factoryConfigurer.configure(factory,connectionFactory);
        //容器工厂创建监听器容器
        SimpleMessageListenerContainer container = factory.createListenerContainer();
        //指定监听器
        container.setMessageListener(orderListener);
        //指定监听器监听的队列
        container.setQueues(orderQueue());
        return container;
    }

    //---------------------------------------pay相关------------------------------------------------
    // 生产者 ---> pay交换机 ---> 死信队列 ---t-t-l---> 死信交换机 ---> pay队列
    /**
     * pay交换机
     * @return
     */
    @Bean
    public DirectExchange payDirectExchange(){
        return new DirectExchange(payExchangeName);
    }

    /**
     * 死信队列
     * 消息在里面过期后会传给指定的交换机
     * @return
     */
    @Bean
    public Queue deadLetterQueue() {
        Map<String, Object> map = new HashMap<>(16);
        // 死信交换机
        map.put("x-dead-letter-exchange", deadLetterExchangeName);
        // 死信routingkey
        map.put("x-dead-letter-routing-key", payRoutingKey);
        // 死信队列中的消息过期时间
        map.put("x-message-ttl", ttl);
        // 死信队列
        return new Queue(deadLetterQueueName, true, false, false, map);
    }

    /**
     * pay交换机与死信队列绑定
     * @return
     */
    @Bean
    public Binding payExAndDeadLetterQueueBinding(Queue deadLetterQueue, DirectExchange payDirectExchange){
        return BindingBuilder.bind(deadLetterQueue).to(payDirectExchange).with(payRoutingKey);
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean
    public DirectExchange deadLetterDirectExchange(){
        return new DirectExchange(deadLetterExchangeName);
    }

    /**
     * pay队列
     * @return
     */
    @Bean
    public Queue payQueue(){
        return new Queue(payQueueName);
    }

    /**
     * 死信交换机与pay队列绑定
     * @return
     */
    @Bean
    public Binding deadLetterExAndPayQueueBinding(Queue payQueue, DirectExchange deadLetterDirectExchange){
        return BindingBuilder.bind(payQueue).to(deadLetterDirectExchange).with(payRoutingKey);
    }

    /**
     * 支付队列监听器容器
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer payMessageListenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        SimpleMessageListenerContainer listenerContainer = factory.createListenerContainer();
        listenerContainer.setMessageListener(payListener);
        listenerContainer.setQueues(payQueue());
        return listenerContainer;
    }
}
