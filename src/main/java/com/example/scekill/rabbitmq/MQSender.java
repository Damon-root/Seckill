package com.example.scekill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

//
//    //fanout 模式
//    public  void send01(Object msg){
//        log.info("发消息"+ msg);
//        rabbitTemplate.convertAndSend("fanoutExchange","",msg);
//    }
//
//    //direct
//    public  void send02(Object msg){
//        log.info("发消息red消息"+ msg);
//        rabbitTemplate.convertAndSend("directExchange","queue.red",msg);
//    }
//
//    public  void send03(Object msg){
//        log.info("发消息green消息"+ msg);
//        rabbitTemplate.convertAndSend("directExchange","queue.green",msg);
//    }

    /*
        发送秒杀信息
     */
    public void sendSeckillMessage(String message){
        log.info("发送消息："+message);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }

}
