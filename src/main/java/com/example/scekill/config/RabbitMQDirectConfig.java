package com.example.scekill.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQDirectConfig {
    //direct模式
    private static  final String QUEUEDirect01 = "queue_direct01";
    private static  final String QUEUEDirect02 = "queue_direct02";
    private static  final String EXCHANGE = "directExchange";
    private static  final  String ROUTINGKEY01 = "queue.red";
    private static  final  String ROUTINGKEY02 = "queue.green";

    @Bean
    public Queue queue03(){
        return new Queue(QUEUEDirect01);
    }

    @Bean
    public Queue queue04(){
        return new Queue(QUEUEDirect02);
    }

    @Bean
    public DirectExchange directExchange(){
        return  new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding03(){
        return BindingBuilder.bind(queue03()).to(directExchange()).with(ROUTINGKEY01);
    }

    @Bean
    public Binding binding04(){
        return BindingBuilder.bind(queue04()).to(directExchange()).with(ROUTINGKEY02);
    }
}
